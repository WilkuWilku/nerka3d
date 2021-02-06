function [compressedLayer] = rleCompressLayer(layerMatrix, layerName)
    compressedLayer = append(layerName, ':');
    for i=1:length(layerMatrix(:,1))
        compressedRow = rleCompressRow(strrep(int2str(layerMatrix(i,:)), ' ', ''));
        compressedLayer = append(compressedLayer, '\n', compressedRow); 
    end
end

