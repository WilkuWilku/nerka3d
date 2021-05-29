function [outputString] = getLayerTranslationString(layerNumber, W)
    xyz = W{layerNumber};
    if(isempty(xyz))
        xyz = [0 0 0];
    end
    % indeksy 1 i 2 celowo zamienione - pliki przesunięć mają zamienione
    % osie X i Y względem naszego układu
    outputString = sprintf('translation %d %d %d\n', xyz(2), xyz(1), xyz(3));
end

