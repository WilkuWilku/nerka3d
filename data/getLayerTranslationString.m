function [outputString] = getLayerTranslationString(layerNumber, W, R)
    currentTranslation = [0 0 0];
    lengthR = length(R);
    lengthW = length(W);
    % comparing number of non-empty elements of R and W
    if(lengthR==lengthW) 
        fprintf('R-W = 0, building translation string...\n');
        for i=lengthR:-1:layerNumber
            xyz = W{i};
            currentTranslation = currentTranslation - xyz;
            fprintf('i=%d, target=%d, layer translation: [%g %g %g], new current translation: [%g %g %g]\n', i, layerNumber, xyz, currentTranslation);
        end
    else
    % todo: eksperymentalnie, do potwierdzenia
        if(lengthR-lengthW==1)
            fprintf('R-W = 1, building translation string...\n');
            if(layerNumber >= 2)
                for i=lengthR:-1:layerNumber
                    xyz = W{i-1};
                    currentTranslation = currentTranslation + xyz;
                    fprintf('i=%d, target=%d, layer translation: [%g %g %g], new current translation: [%g %g %g]\n', i, layerNumber, xyz, currentTranslation);
                end
            end
        else
            fprinf('ERROR: Invalid align file - layers number does not match\n');
        end
    end
    % indeksy 1 i 2 celowo zamienione - pliki przesunięć mają zamienione
    % osie X i Y względem naszego układu
    outputString = sprintf('translation %d %d %d\n', currentTranslation(2), currentTranslation(1), currentTranslation(3));
end

