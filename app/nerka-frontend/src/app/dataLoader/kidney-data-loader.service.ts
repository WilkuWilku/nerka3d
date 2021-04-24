import {Injectable} from '@angular/core';
import {CtlReaderService} from "./ctl-reader.service";
import {RawLayer} from "../dto/rawLayer";
import {KidneyLayerData} from "../dto/kidneyLayerData";
import {BufferGeometry, Points, PointsMaterial, Vector3} from "three";
import {ObjectType} from "../dto/objectType";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class KidneyDataLoaderService {

  constructor(private ctlReaderService: CtlReaderService) { }

  public loadKidneyData(files, pointsReductionRatio: number, distanceBetweenLayers: number): Observable<any> {
    return new Observable(subscriber => {
      const filesLoaded: boolean[] = new Array(files.length).fill(false);
      for (let i = 0; i < files.length; i++) {
        const fileReader = new FileReader();
        fileReader.onload = () => {
          if(files[i].name.endsWith('.ctl')) {
            const rawLayer: RawLayer = this.ctlReaderService.parseLayer(fileReader.result.toString())
            const layerData: KidneyLayerData = this.createKidneyLayerData(rawLayer, pointsReductionRatio, distanceBetweenLayers);
            filesLoaded[i] = true;
            subscriber.next(layerData);
          } else if(files[i].name.endsWith('.json')) {
            const translationDataFromFile = JSON.parse(fileReader.result.toString())
            let layersTranslationsData: Map<number, Array<number>>; // Map<layerNumber, [X, Y, Z]>
            layersTranslationsData = this.loadLayersTranslations(translationDataFromFile);
            filesLoaded[i] = true;
            subscriber.next(layersTranslationsData);
          }
          if (filesLoaded.every(loaded => loaded === true)) {
            subscriber.complete();
          }
        }
        fileReader.readAsText(files[i]);
      }
    });
  }

  private createKidneyLayerData(rawLayer: RawLayer, pointsReductionRatio: number, distanceBetweenLayers: number): KidneyLayerData {
    const pointsCoords : Vector3[] = [];
    const layerHeight = parseInt(rawLayer.header.layerNumber) * distanceBetweenLayers;
    const layerData = rawLayer.data;

    for (let i = 1; i < layerData.length - 1; i++) {
      for (let j = 1; j < layerData[i].length - 1; j++) {
        if (this.isVisibleBorderPoint(layerData, i, j, pointsReductionRatio)) {
          pointsCoords.push(new Vector3(i, layerHeight, j));
        }
      }
    }

    const pointsGeometry = new BufferGeometry().setFromPoints(pointsCoords);
    const objectType = ObjectType.fromString(rawLayer.header.objectType);
    const material = new PointsMaterial({color: this.getPointColor(objectType), size: 10});
    const points = new Points(pointsGeometry, material)
    return new KidneyLayerData(points, rawLayer.header.fileName, objectType, layerHeight, rawLayer)
  }

  private isVisibleBorderPoint(jsonData, i, j, pointsReductionRatio): boolean {
    return jsonData[i][j] === 1
      && (i * jsonData.length + j) % pointsReductionRatio === 0
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

  private getPointColor(objectType: ObjectType) {
    if (objectType === ObjectType.Kidney) {
      return 0x0000ff;
    }
    if (objectType === ObjectType.Cancer) {
      return 0xff0000;
    }
    // undefined object
    return 0x696969;
  }

  private loadLayersTranslations(translationsData: Array<Array<number>>): Map<number, Array<number>>{
    let layersTranslationsData: Map<number, Array<number>> = new Map(); // Map<layerNumber, [X, Y, Z]>
    translationsData.forEach((value, index) => {
      layersTranslationsData.set(index, value);
    })
    return layersTranslationsData;
  }

}
