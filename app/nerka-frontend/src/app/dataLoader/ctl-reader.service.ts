import { Injectable } from '@angular/core';
import {RawLayer, RawLayerHeader} from "../dto/rawLayer";


@Injectable({
  providedIn: 'root'
})
export class CtlReaderService {

  constructor() { }

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

  public parseLayer(compressedLayerData: string): RawLayer {
    console.log("parseLayer START")
    let layerData = [];
    const layerElements = compressedLayerData.split("\n");
    const layerHeaderString = layerElements[0].split(".ctl")[0];

    layerElements.slice(1).forEach(value => {
      const rowData = this.parseRow(value);
      layerData.push(rowData);
    });
    console.log("parseLayer layerHeaderString", layerHeaderString)
    const [fileName, objectType, layerIndexData] = layerHeaderString.split("_");
    console.log("parseLayer parsed", fileName, objectType, layerIndexData)
    const [layerNumber, multipliedLayerIndex] = layerIndexData.split("-");
    console.log("parseLayer data", layerData)
    const layerHeader = new RawLayerHeader(fileName, objectType, layerNumber, multipliedLayerIndex);
    console.log("parseLayer END")
    return new RawLayer(layerHeader,  layerData);
  }

}
