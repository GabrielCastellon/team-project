# Discovery UML and skeleton integration

## What each diagram covers

A **class diagram** shows types, their responsibilities, and their dependencies. A **sequence diagram** shows the objects taking part in one particular interaction, in time order. It does not need every project class. Every application participant in the search sequence below appears in the search class diagram, startup and recommendation/persistence types have separate diagrams. JDK Swing/HTTP internals, Gson and ONNX internals are omitted for readability.

The previous diagram covered only the three entity types. That was an entity diagram, not enough to explain the complete discovery use case or its integration with the skeleton.

## 1. Search use-case class diagram

```mermaid
classDiagram
    direction LR
    class DiscoveryView {
        +setController(DiscoveryController controller)
        +busy(boolean value)
        +show(List~Card~ cards, String message)
        +status(String message)
    }
    class DiscoveryController {
        -DiscoveryInputBoundary service
        -DiscoveryPresenter presenter
        +search(String query, int page, String orientation, String order)
    }
    class DiscoveryInputBoundary {
        <<interface>>
        +search(String query, int page, String orientation, String order) List~Photo~
    }
    class DiscoveryService {
        -ImageCatalog catalog
        +search(String query, int page, String orientation, String order) List~Photo~
    }
    class ImageCatalog {
        <<interface>>
        +search(String query, int page, String orientation, String order) List~Photo~
        +trackSelection(Photo photo)
    }
    class UnsplashCatalog {
        +search(String query, int page, String orientation, String order) List~Photo~
        +trackSelection(Photo photo)
    }
    class DiscoveryPresenter {
        -ThumbnailLoader thumbnails
        -View view
        +cards(List~Photo~ photos, List~String~ scores) List~Card~
        +show(List~Card~ cards, String message)
        +busy(boolean value)
        +status(String message)
    }
    class PresenterView {
        <<interface>>
        +busy(boolean value)
        +show(List~Card~ cards, String message)
        +status(String message)
    }
    class ThumbnailLoader {
        <<interface>>
        +load(String url) BufferedImage
    }
    class ImageLoader {
        +load(String url) BufferedImage
    }
    class Card {
        +Photo photo
        +BufferedImage thumbnail
        +String score
    }
    class Photo
    DiscoveryView --> DiscoveryController : forwards input
    DiscoveryController --> DiscoveryInputBoundary : calls
    DiscoveryService ..|> DiscoveryInputBoundary
    DiscoveryService --> ImageCatalog
    UnsplashCatalog ..|> ImageCatalog
    DiscoveryService ..> Photo : returns
    UnsplashCatalog ..> Photo : constructs
    DiscoveryController --> DiscoveryPresenter
    DiscoveryPresenter --> PresenterView : publishes UI changes
    DiscoveryView ..|> PresenterView
    DiscoveryPresenter --> ThumbnailLoader
    DiscoveryPresenter ..> Card : creates
    Card --> Photo
    note for PresenterView "Actual nested type: DiscoveryPresenter.View"
    note for ThumbnailLoader "Actual nested type: DiscoveryPresenter.ThumbnailLoader, bound to images::load in DiscoveryModule"
    note for Card "Actual nested type: DiscoveryPresenter.Card"
    note for ImageLoader "Supplied through the ThumbnailLoader method reference, does not implement that interface directly"
```

This is a search slice: only the search operation is listed on the input boundary/service, their recommendation operations and ports are shown below. Arrows are source dependencies, not a claim that the service calls the presenter. `DiscoveryService` returns data through `DiscoveryInputBoundary`, the controller then uses `DiscoveryPresenter`. This synchronous response design is an explicit variation from an interactor calling an output-boundary callback. No separate output boundary or view-model class is invented in this diagram.

## 2. Successful search sequence

Precondition: the Discovery tab is already initialized. Everything before the background-thread note runs on Swing's event dispatch thread (EDT). The background search task and its completion callback are owned by the controller's anonymous `SwingWorker`, the worker and JDK executor are omitted as separate lifelines.

```mermaid
sequenceDiagram
    actor User
    participant V as view:DiscoveryView
    participant C as controller:DiscoveryController
    participant U as service:DiscoveryService
    participant A as catalog:UnsplashCatalog
    participant P as presenter:DiscoveryPresenter
    participant L as images:ImageLoader
    User->>V: Click Search
    V->>C: search(query, page, orientation, order)
    C->>C: run(task, message), reject overlapping action
    C->>P: busy(true), status(Working)
    P->>V: busy(true), status(Working)
    Note over C,L: SwingWorker.doInBackground invokes the search task
    C->>C: Clear previous candidates
    C->>U: search(...) through DiscoveryInputBoundary
    U->>U: Validate query, page, orientation and order
    U->>A: search(...) through ImageCatalog
    A->>A: Request Unsplash and map JSON to Photo objects
    A-->>U: List of Photo
    U-->>C: Immutable list of Photo
    C->>P: cards(found, null)
    loop Each result
        P->>L: load(imageUrl) through ThumbnailLoader method reference
        alt Cached or downloaded preview available
            L-->>P: BufferedImage
        else Preview unavailable
            L-->>P: Throw exception
            P->>P: Retain Photo with null thumbnail
        end
        P->>P: Construct Card
    end
    P-->>C: List of Card
    C->>C: Store found as current candidates
    Note over C,V: SwingWorker.done executes on EDT
    C->>P: show(cards, success or empty-results message)
    P->>V: show(cards, message)
    C->>P: busy(false)
    P->>V: busy(false)
```

`Photo` and `Card` are returned data objects, so they do not need lifelines of their own here. `FeatureVector`, embedding providers and the preference store are not called by keyword search, they belong in recommendation/save sequences. `Main`, the application factory and lazy shell panels belong in the startup sequence below, not in every search interaction.

## 3. Search failure sequence

```mermaid
sequenceDiagram
    participant C as controller:DiscoveryController
    participant U as service:DiscoveryService
    participant A as catalog:UnsplashCatalog
    participant P as presenter:DiscoveryPresenter
    participant V as view:DiscoveryView
    Note over C,V: Controls already disabled, worker clears previous candidates
    C->>U: search(...) on background thread
    alt Invalid request
        U-->>C: Throw validation exception, catalog not called
    else Valid request but API fails
        U->>A: search(...)
        A-->>U: Throw IOException
        U-->>C: Propagate IOException
    end
    Note over C,V: SwingWorker.done receives ExecutionException on EDT
    C->>P: status(Could not complete + error detail)
    P->>V: status(error)
    C->>P: busy(false)
    P->>V: busy(false)
```

No cards are built and candidates remain empty after validation/API failure. A preview-only failure follows the successful-search sequence and keeps the photo/attribution available. Cancellation has its own `InterruptedException` handling in the controller.

## 4. Skeleton startup and integration

```mermaid
classDiagram
    class Main
    class ApplicationFactory {
        +createView() MainView
    }
    class MainView
    class LazyPanel {
        +activate()
    }
    class PlaceholderView
    class DiscoveryModule {
        +createPanel() JPanel$
    }
    class DiscoveryView
    class DiscoveryController
    class DiscoveryService
    Main ..> ApplicationFactory
    ApplicationFactory ..> MainView : constructs shared shell
    ApplicationFactory ..> DiscoveryModule : Discovery panel supplier
    ApplicationFactory ..> PlaceholderView : other owners' tabs
    MainView *-- LazyPanel : one per tab
    LazyPanel o-- DiscoveryView : retains selected Discovery content
    DiscoveryModule ..> DiscoveryView : constructs
    DiscoveryModule ..> DiscoveryController : constructs and attaches
    DiscoveryModule ..> DiscoveryService : constructs with adapters
```

```mermaid
sequenceDiagram
    participant M as Main
    participant F as ApplicationFactory
    participant S as MainView
    participant L as LazyPanel
    participant D as DiscoveryModule
    participant V as DiscoveryView
    M->>F: createView() on EDT
    F->>S: Construct tabs with panel suppliers
    Note over S,L: Accounts opens first, Discovery is not created yet
    S-->>M: Shared shell panel
    Note over M: Set JFrame content, show shared window
    Note over S: User selects Discovery
    S->>L: activate()
    alt First successful activation
        L->>D: createPanel() through supplier.get()
        D->>D: Wire catalog, embeddings, preference store and service
        D->>V: Construct view
    D->>D: Construct controller and presenter with this view
    D->>V: setController(controller)
        V-->>D: JPanel
        D-->>L: Discovery panel
        L->>L: Cache and display panel
    else Already initialized
        L->>L: Reuse existing panel and state
    end
```

If module construction fails, `LazyPanel` shows the configuration error and Retry while other tabs remain usable. Optional CLIP initialization can take time on first activation, the proxy delays construction but does not make it asynchronous.

The app starts at `app.Main` from Maven, uses the skeleton's `MainView` and `LazyPanel`, and replaces only the Discovery factory. There is no restored standalone `DiscoveryApplication` entry point and no runtime checkout, read or dependency on a Git branch.

`kevin_working` is an unchanged historical backup at `1cdd12d817b281ba80dbe2d84c8cc379e8a27f1a`. The shared skeleton was published first at `01a32869d53ef368c899fb44600ad0a95b52b528`, PR #10 is a descendant of that commit. Run the PR branch to exercise the integrated feature, do not run or merge the backup as the active application.

## 5. Recommendation, embedding and persistence dependencies

```mermaid
classDiagram
    class DiscoveryService
    class EmbeddingProvider {
        <<interface>>
        +modelId() String
        +embed(Photo photo) FeatureVector
    }
    class PreferenceStore {
        <<interface>>
        +load(String profile) List~Photo~
        +save(String profile, List~Photo~ photos)
    }
    class CachedEmbeddingProvider
    class ColorEmbeddingProvider
    class ClipEmbeddingProvider
    class JsonPreferenceStore
    class ImageLoader
    class FeatureVector
    class Photo
    class ScoredPhoto
    DiscoveryService --> EmbeddingProvider
    DiscoveryService --> PreferenceStore
    DiscoveryService ..> FeatureVector : aggregate and compare
    DiscoveryService ..> ScoredPhoto : rank results
    CachedEmbeddingProvider ..|> EmbeddingProvider
    CachedEmbeddingProvider o-- EmbeddingProvider : delegates and caches
    ColorEmbeddingProvider ..|> EmbeddingProvider
    ClipEmbeddingProvider ..|> EmbeddingProvider
    ColorEmbeddingProvider --> ImageLoader
    ClipEmbeddingProvider --> ImageLoader
    JsonPreferenceStore ..|> PreferenceStore
    JsonPreferenceStore ..> Photo : serialize and reload
    EmbeddingProvider ..> Photo
    EmbeddingProvider ..> FeatureVector
    ScoredPhoto --> Photo
```

The default embedding strategy is a normalized RGB histogram, optional CLIP uses an external compatible model. `CachedEmbeddingProvider` is a Decorator over either provider. Real account ownership, pins and boards remain their assigned teammates' work, demo preference IDs are not authentication or a board repository.

## 6. Entity detail

```mermaid
classDiagram
    class Photo {
        -String id
        -String description
        -String imageUrl
        -String pageUrl
        -String photographer
        -String photographerUrl
        -String downloadUrl
        +id() String
        +description() String
        +imageUrl() String
        +pageUrl() String
        +photographer() String
        +photographerUrl() String
        +downloadUrl() String
    }
    class FeatureVector {
        -double[] values
        +values() double[]
        +cosine(FeatureVector other) double
        +mix(FeatureVector other, double weight) FeatureVector
        +average(Collection~FeatureVector~ vectors) FeatureVector$
    }
    class ScoredPhoto {
        -Photo photo
        -double score
        +photo() Photo
        +score() double
    }
    ScoredPhoto --> "1" Photo : wraps
```

`Photo` carries Unsplash metadata and attribution. `FeatureVector` validates and normalizes vectors and returns copies of its values. `ScoredPhoto` pairs metadata with a similarity score. Cosine similarity is not a probability or percentage accuracy.
