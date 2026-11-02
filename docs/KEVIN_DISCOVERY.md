# Kevin's image discovery use cases

## Scope and status

Implements README stories 2 (search/browse) and 3 (recommendations), plus Taste Mixer, group ranking and preference-board analysis. Java 11, Swing, Unsplash and JSON. This is a standalone module/panel because the repository had no application code at implementation time. It does not implement accounts, pin CRUD, board CRUD or profiles owned by teammates.

The complete baseline workflow runs with an Unsplash access key. The optional CLIP adapter compiles and its preprocessing is unit tested; pretrained model export/inference and live Unsplash access still need the manual checks below. Colour mode is a real visual histogram, not semantic CLIP. Never describe the baseline as recognizing room styles or objects.

## Run in VS Code (Windows PowerShell)

Install a JDK 11 or 17, Maven, and VS Code's Extension Pack for Java. Open the repository folder. In its terminal:

```powershell
mvn test
$env:UNSPLASH_ACCESS_KEY = "your-own-Unsplash-access-key"
mvn exec:java
```

Use an Unsplash **Access Key**, not its Secret Key. Never commit either. Restart the app after changing environment variables. For macOS/Linux use `export UNSPLASH_ACCESS_KEY='...'` and the same Maven commands.

The app starts in `rgb-histogram-64-v1` mode unless `CLIP_MODEL_PATH` is set. JSON snapshots live under `data/discovery-preferences/` (gitignored). `DISCOVERY_DATA_DIR` overrides that directory. Use one running app per data directory; this local adapter does not provide cross-process locking or account authorization.

## Demo workflow

1. Search `bedroom`. Pick orientation and relevance/latest order; change Page and Search to browse further.
2. Like several results into A. Search another style and like images into B. These are preference samples, separate from your teammates' saved pins.
3. Search a fresh candidate page, then Recommend A. Already-liked images are excluded.
4. Adjust the A-weight slider to 70, then Mix A + B. Move toward 0 to favour B or 100 to favour A.
5. Enter two or more comma-separated preference IDs and choose Recommend for group. Every member needs samples.
6. Show A or Show B to inspect persistence, or remove samples. Restart and show the same ID to verify reload.
7. Analyze A after adding at least two samples. Lowest cosine similarity to the centroid is first; these are potential outliers, not automatically incorrect pins. A two-image board will normally tie.

Ranking operates on the most recently searched **page of 20 candidates**, not all of Unsplash. A new keyword in the text field only takes effect when Search is clicked. An empty result may mean all candidates were already liked. There is no fabricated personalized cold start: empty preference collections produce an actionable message. Image/embedding failure fails ranking instead of silently ranking an incomplete candidate set. Individual thumbnail failure retains attribution and displays “Preview unavailable”.

## Optional semantic CLIP

One-time setup in a separate Python environment (downloads pretrained weights; several hundred MB):

```powershell
python -m venv .venv-clip
.\.venv-clip\Scripts\Activate.ps1
pip install -r scripts/clip-requirements.txt
python scripts/export_clip.py
$env:CLIP_MODEL_PATH = (Resolve-Path models/clip-vit-b32-image.onnx).Path
mvn exec:java
```

The exporter produces the **projected image features** of OpenAI CLIP ViT-B/32. The Java adapter expects one float input `pixel_values` of shape `[1,3,224,224]` and a float output `image_embeds` of shape `[1,512]`. It rejects full image+text graphs or incompatible exports. Do not use arbitrary ONNX models with this adapter.

Java preprocessing resizes the shortest edge to 224 with bicubic interpolation, centre crops to 224, converts RGB to CHW floats, and applies the model's mean/std. Java2D's bicubic interpolation can differ slightly from the original PIL preprocessing; do not claim bit-identical parity. Unit normalization occurs in `FeatureVector`. No text encoder is needed for image-to-image similarity. The input key/weights/model files are not stored in Git. Clear `CLIP_MODEL_PATH` to return to colour mode; the app never silently falls back if a requested CLIP model fails.

References: [ONNX Java](https://onnxruntime.ai/docs/get-started/with-java.html), [CLIP model](https://huggingface.co/openai/clip-vit-base-patch32), [CLIP preprocessing](https://huggingface.co/openai/clip-vit-base-patch32/blob/main/preprocessor_config.json).

## Architecture and team integration

| Layer | Main types | Responsibility |
|---|---|---|
| Entities | `Photo`, `FeatureVector`, `ScoredPhoto` | Immutable source metadata, validated vector math, ranked result |
| Use cases | `DiscoveryService` | Search validation, preference aggregation, ranking, mixing, group scores, analysis |
| Ports | `ImageCatalog`, `EmbeddingProvider`, `PreferenceStore` | Abstract external search, image encoding and preference persistence |
| Adapters | `UnsplashCatalog`, `JsonPreferenceStore`, embedding providers | HTTP/JSON/model infrastructure |
| UI adapter | `DiscoveryController`, `Card` | Background execution, presentation models, success/error status |
| View | `DiscoveryView` | Swing input and image grid; no HTTP, JSON or vector calculations |
| Composition | `DiscoveryApplication` | Chooses adapters and wires dependencies |

Dependency inversion: the service owns its interfaces and knows nothing about Gson, ONNX or Swing. Strategy: colour and CLIP providers implement the same interface. Decorator: `CachedEmbeddingProvider` adds bounded caching without changing either provider. The controller uses a view boundary, allowing a fake view in tests. Swing widgets remain in the outer layer.

Gabriel: supply stable namespaced board IDs (for example `user:42/board:17`) and implement `PreferenceStore` over your board/pin repositories, mapping pins to `Photo`. That adapter can expose actual board pins to the analyzer without copying board CRUD here. Alex: retain Unsplash metadata when saving a pin; use `ImageCatalog.trackSelection` for actual selections as appropriate, and do not double count if the action already passes through `DiscoveryService.like`. Matthew: supply the authenticated user's scoped IDs and enforce ownership in the adapter. The standalone demo's editable IDs are **not authentication**.

To embed the panel, construct `DiscoveryView`, construct `DiscoveryController(service, imageLoader::load, view)`, call `view.setController(controller)`, then add the panel to the shared application. Keep shared startup/account navigation in the team's composition root.

## Math you should be able to explain

Each embedding is unit length. A board/user preference is the normalized average of its sample embeddings, giving each image equal weight. Cosine is the dot product of unit vectors, in `[-1,1]` (colour histograms are nonnegative). It is a similarity score, not a probability or percentage accuracy.

Taste Mixer normalizes `w*A + (1-w)*B`; at `w=.7` this is the requested `0.7A + 0.3B`. Endpoints need only the active board. Opposite vectors that cancel to zero are rejected because they have no direction.

Group score is `min(cos(candidate, member))`. Ranking that score descending favours the least dissatisfied member (“least misery”); it does not guarantee that any image is liked by everyone. A mean score could conceal one person's very poor match. Board analysis sorts ascending against the board centroid, including each pin in that centroid, exactly as specified. There is no arbitrary outlier threshold.

Ties use photo ID for deterministic ordering. Candidates are deduplicated and saved sample IDs excluded. Invalid vectors (empty, zero, nonfinite or incompatible dimensions) are rejected. Averaging uses normalized embeddings so a larger raw embedding does not dominate.

## Persistence, API behaviour and limitations

Preference JSON contains photo metadata, not downloaded image files. Atomic file replacement prevents partially-written JSON. IDs are SHA-256 filenames to avoid path traversal. Corrupt data causes a visible error rather than silent reset. Embeddings are cached in memory per provider instance, then recomputed after restart; this avoids stale vectors when switching models.

Unsplash image URLs are hotlinked, photographer/source links include attribution parameters, and selection calls the returned `download_location`. The credential is sent only to the configured API host. HTTP requests have timeouts and errors distinguish invalid keys and access/rate limits. Search has relevance/latest and orientation filters; Unsplash search does not offer a popularity ordering, so it is not invented. See [Unsplash documentation](https://unsplash.com/documentation).

The baseline cannot understand semantic style. CLIP quality still needs evaluation on representative boards. Sequential image fetching favours simplicity over throughput; all such work is off the EDT. No shared account sync, multi-process edits, background recommendation refresh or automatic board integration is claimed. Cancellation and broader caching can be future extensions.

## Verification

Run `mvn test` or `mvn verify`. Tests use generated images, fake gateways and a loopback HTTP server; no key, live network or pretrained model is required. They cover math, recommendation order, exclusions, weighting endpoints, group compromise, outliers, failed saves, JSON restart/corruption, Unsplash requests/errors/tracking, preprocessing and embedding caching. GitHub Actions runs Java 11 and 17.

Manual checks before the team demo:
- Start with a real key and verify thumbnails/attribution, orientation, latest order and page changes.
- Build A/B preferences and exercise all five actions: personal, mix, group, show, analyze.
- Restart to verify saved samples, and remove one to verify preference changes.
- Try an empty preference ID and a bad key; check clear errors and responsive UI.
- Export the optional CLIP model, run the same workflow, and verify the model label and plausible semantic ranking.
- Integrate the panel with the team's actual board/pin adapters before calling this the complete group application.
