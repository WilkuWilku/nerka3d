function [compressedRow] = rleCompressRow(inputRow)
    compressedRow = "";
     % count zeros
       [startIndex, endIndex] = regexp(inputRow, '0*', 'once', 'start', 'end'); 
       if ~(isempty(startIndex) || isempty(endIndex) || startIndex > 1)
           %fprintf('init counting 0: start=%d, end=%d, input=%s\n', startIndex, endIndex, inputRow); 
            compressedRow = append(compressedRow, int2str(endIndex-startIndex+1));
            inputRow = extractAfter(inputRow, endIndex);
            
       else
           compressedRow = append(compressedRow, '0');
       end
       
    
    while ~isempty(inputRow)
       % count ones
       [startIndex, endIndex] = regexp(inputRow, '1*', 'once', 'start', 'end'); 
       if ~(isempty(startIndex) || isempty(endIndex))
           %fprintf('counting 1: start=%d, end=%d, input=%s\n', startIndex, endIndex, inputRow); 
            compressedRow = append(compressedRow, ';', int2str(endIndex-startIndex+1));
            inputRow = extractAfter(inputRow, endIndex);
       else
           compressedRow = append(compressedRow, ';0');
       end
       
       if ~isempty(inputRow)
           % count zeros
           [startIndex, endIndex] = regexp(inputRow, '0*', 'once', 'start', 'end'); 
           if ~(isempty(startIndex) || isempty(endIndex))
               %fprintf('counting 0: start=%d, end=%d, input=%s\n', startIndex, endIndex, inputRow); 
                compressedRow = append(compressedRow, ';', int2str(endIndex-startIndex+1));
                inputRow = extractAfter(inputRow, endIndex);
           else
               compressedRow = append(compressedRow, ';0');
           end
       end
    end
end

