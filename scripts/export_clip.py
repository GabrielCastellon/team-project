from pathlib import Path
import torch
from transformers import CLIPVisionModelWithProjection

class ImageEncoder(torch.nn.Module):
    def __init__(self, model):
        super().__init__()
        self.model = model

    def forward(self, pixel_values):
        return self.model(pixel_values=pixel_values).image_embeds

if __name__ == "__main__":
    model = CLIPVisionModelWithProjection.from_pretrained(
        "openai/clip-vit-base-patch32", revision="3d74acf9a28c67741b2f4f2ea7635f0aaf6f0268", attn_implementation="eager"
    ).eval()
    destination = Path("models/clip-vit-b32-image.onnx")
    destination.parent.mkdir(parents=True, exist_ok=True)
    with torch.no_grad():
        torch.onnx.export(
            ImageEncoder(model).eval(), (torch.zeros(1, 3, 224, 224),), str(destination),
            input_names=["pixel_values"], output_names=["image_embeds"], opset_version=17,
        )
    print(destination.resolve())
