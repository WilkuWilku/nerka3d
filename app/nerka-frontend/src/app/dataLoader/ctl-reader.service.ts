import { Injectable } from '@angular/core';
import {BufferGeometry, Points, PointsMaterial, Vector3} from "three";
import {Constants} from "../constants/constants";
import {Layer, LayerHeader} from "../dto/layer";

const pointsDensityReductionRatio = 10;
const layerHeight = 100;


@Injectable({
  providedIn: 'root'
})
export class CtlReaderService {

  constructor() { }

  public convertCtlFileToPoints(files): [Points, number]{
    const points: Points = new Points();
    let pointsCount = 0;
    for (let i = 0; i < files.length; i++) {
      const fileReader = new FileReader();
      fileReader.readAsText(files[i]);
      fileReader.onload = () => {
        let layer: Layer = this.parseLayer(fileReader.result.toString())
        const [pointsFromFile, pointsCountFromFile] = this.createLayerPoints(
          layer, pointsDensityReductionRatio, parseInt(layer.header.layerNumber) * layerHeight);
        pointsCount += pointsCountFromFile;
        points.add(pointsFromFile);
      }
    }
    return [points, pointsCount];
  }

  public parseRow(compressedRowData: string): number[] {
    let rowData = [];
    const valueLengths = compressedRowData.split(";");
    valueLengths.forEach((value, index) => {
      let rowFragment;
      if(index % 2 == 0) {
        const zerosLength = parseInt(value);
        rowFragment = new Array(zerosLength).fill(0);
      } else {
        const onesLength = parseInt(value);
        rowFragment = new Array(onesLength).fill(1);
      }
      rowData.push(...rowFragment);
    })
    return rowData;
  }

  public parseLayer(compressedLayerData: string): Layer {
    let layerData = [];
    const layerElements = compressedLayerData.split("\n");
    const layerHeaderString = layerElements[0];

    layerElements.slice(1).forEach(value => {
      const rowData = this.parseRow(value);
      layerData.push(rowData);
    });

    const [fileName, objectType, layerIndexData] = layerHeaderString.split("_");
    const [layerNumber, multipliedLayerIndex] = layerIndexData.split("-");

    const layerHeader = new LayerHeader(fileName, objectType, layerNumber, multipliedLayerIndex);

    return new Layer(layerHeader,  layerData);
  }

//*****************************//

  private createLayerPoints(layer: Layer, pointsDensityReductionRatio, layerHeight): [Points, number] {
    const pointsCoords = [];
    const layerData = layer.data;
    for (let i = 1; i < layerData.length - 1; i++) {
      for (let j = 1; j < layerData[i].length - 1; j++) {
        const shouldAdd = this.isVisibleBorderPoint(layerData, i, j, pointsDensityReductionRatio);
        if (shouldAdd) {
          pointsCoords.push(new Vector3(i, layerHeight, j));
        }
      }
    }
    const pointsCount = pointsCoords.length;
    const pointsGeometry = new BufferGeometry().setFromPoints(pointsCoords);
    return [new Points(pointsGeometry, new PointsMaterial({color: this.getPointColor(layer.header.objectType), size: 10})), pointsCount];
  }

  private isVisibleBorderPoint(jsonData, i, j, pointsDensityReductionRatio): boolean {
    return jsonData[i][j] === 1
      && (i * jsonData.length + j) % pointsDensityReductionRatio === 0
      && this.isBorder([
        jsonData[i - 1][j - 1],
        jsonData[i - 1][j],
        jsonData[i - 1][j + 1],
        jsonData[i][j - 1],
        jsonData[i][j + 1],
        jsonData[i + 1][j - 1],
        jsonData[i + 1][j],
        jsonData[i + 1][j + 1],
      ]);
  }

  private isBorder(pointNeighbourhood): boolean {
    for(let i=0; i<pointNeighbourhood.length;i++) {
      if(pointNeighbourhood[i] === 0) {
        return true;
      }
    }
    return false;
  }

  private getPointColor(objectType: string) {
    if (objectType === Constants.OBJECT_TYPE_KIDNEY) {
      return 0x0000ff;
    }
    if (objectType === Constants.OBJECT_TYPE_CANCER) {
      return 0xff0000;
    }
    // undefined object
    return 0x696969;
  }


}
