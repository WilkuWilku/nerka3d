import {BufferGeometry, Points, PointsMaterial, Vector3} from "three";


export class DataLoader {

  static convertJsonToKidneyLayers(event, pointsDensityReductionRatio, layerSpacing): [Points, number] {
    const selectedFiles = event.target.files;
    const points: Points = new Points();
    let pointsCount = 0;
    for (let i = 0; i < selectedFiles.length; i++) {
      const fileReader = new FileReader();
      const selectedFile = selectedFiles[i];
      fileReader.readAsText(selectedFile);
      fileReader.onload = () => {
        const jsonData = JSON.parse(fileReader.result.toString());
        const [pointsFromFile, pointsCountFromFile] = this.createLayer(
          jsonData, pointsDensityReductionRatio, this.getFileIndex(selectedFile.name) * layerSpacing);
        pointsCount += pointsCountFromFile;
        points.add(pointsFromFile);
      };
    }
    return [points, pointsCount];
  }

  private static createLayer(jsonData, pointsDensityReductionRatio, layerHeight): [Points, number] {
    const pointsCoords = [];
    for (let i = 1; i < jsonData.length - 1; i++) {
      for (let j = 1; j < jsonData[i].length - 1; j++) {
        const shouldAdd = this.shouldAddPoint(jsonData, i, j, pointsDensityReductionRatio);
        if (shouldAdd) {
          pointsCoords.push(new Vector3(i, layerHeight, j));
        }
      }
    }
    const pointsCount = pointsCoords.length;
    const pointsGeometry = new BufferGeometry().setFromPoints(pointsCoords);
    return [new Points(pointsGeometry, new PointsMaterial({color: 0x0000ff, size: 10})), pointsCount];
  }

  private static shouldAddPoint(jsonData, i, j, pointsDensityReductionRatio): boolean {
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

  private static isBorder(pointNeighbourhood): boolean {
    let counter = 0;
    pointNeighbourhood.forEach(point => {
      if (point === 0) {
        counter++;
      }
    });
    return counter > 0;
  }

  private static getFileIndex(fileName: string): number {
    return Number(fileName.split('.json')[0].split('_')[1]);
  }
}
