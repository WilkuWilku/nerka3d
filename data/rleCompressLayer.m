function [compressedLayer] = rleCompressLayer(layerMatrix, layerName)
    compressedLayer = append(layerName, ':\n');
    for i=1:length(layerMatrix(:,1))
        compressedRow = rleCompressRow(strrep(int2str(layerMatrix(i,:)), ' ', ''));
        compressedLayer = append(compressedLayer, compressedRow, '\n'); 
    end
end

