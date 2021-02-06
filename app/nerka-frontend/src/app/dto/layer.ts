export class Layer {

  constructor(header: LayerHeader, data: number[][]) {
    this.header = header;
    this.data = data;
  }

  header: LayerHeader
  data: number[][]
}

export class LayerHeader {
  constructor(fileName: string, objectType: string, layerNumber: string, multipliedLayerIndex: string) {
    this.fileName = fileName;
    this.objectType = objectType;
    this.layerNumber = layerNumber;
    this.multipliedLayerIndex = multipliedLayerIndex;
  }

  fileName: string
  objectType: string
  layerNumber: string
  multipliedLayerIndex: string
}
