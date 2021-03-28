export class RawLayer {

  constructor(header: RawLayerHeader, data: number[][]) {
    this.header = header;
    this.data = data;
  }

  header: RawLayerHeader
  data: number[][]
}

export class RawLayerHeader {
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
