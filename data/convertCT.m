% przykladowa konwersja z data/ct7b.mat -> data/output/ct7b_typ_x-y.json, gdzie
% typ -> jaki obiekt przedstawia mapa - nerka / cancer
% x -> numer warstwy
% y -> numer skanu dla danego typu i warstwy

function convertCT(ct_input_file_name, layer_translation_file_name)
    fprintf('Processing file: %s\n', ct_input_file_name);
    output_file_name_base = extractBefore(ct_input_file_name, '.mat');
    
    if ~exist('output')
        fprintf('Directory "output" does not exist. Creating new...\n');
        mkdir('output')
    end
    
    if ~exist(strcat('output/',output_file_name_base))
        fprintf('Directory "output/%s" does not exist. Creating new...\n', output_file_name_base);
        mkdir(strcat('output/',output_file_name_base))
    end 
    
    fprintf('Loading align file: %s\n', layer_translation_file_name);
    load(layer_translation_file_name);
    
    load(ct_input_file_name);
    result = {};
    layer_number = 0;
    for i = 1:length(R)
       if ~isempty(R{i})
           kidney_layer_index = 0;
           cancer_layer_index = 0;
           is_layer_counted = false;

           for j = 1:length(R{i})
               if ~isempty(R{i}{j})
                   if(~is_layer_counted)
                        layer_number = layer_number + 1;
                        is_layer_counted = true;
                   end

                   for k=1:(length(R{i}{j})-1)
                       % nerka
                       if ~isempty(R{i}{j}{k}) && strcmp(R{i}{j}{k+1},'nerka')
                           result = R{i}{j}{k};
                           new_output_file_name = sprintf('%s_kidney_%d-%d.ctl', output_file_name_base, layer_number, kidney_layer_index);
                           fprintf('Adding R{%d}{%d}{%d} to file %s\n', i, j, k, strcat('output/',output_file_name_base,'/',new_output_file_name));
                           output_file = fopen(strcat('output/',output_file_name_base,'/',new_output_file_name), 'w');
                           fprintf(output_file, getLayerTranslationString(i, W));
                           fprintf(output_file, rleCompressLayer(result, new_output_file_name));
                           kidney_layer_index = kidney_layer_index + 1;
                           fclose(output_file);
                       end

                       % rak
                       if ~isempty(R{i}{j}{k}) && strcmp(R{i}{j}{k+1},'Cancer')
                           result = R{i}{j}{k};
                           new_output_file_name = sprintf('%s_cancer_%d-%d.ctl', output_file_name_base, layer_number, cancer_layer_index);
                           fprintf('Adding R{%d}{%d}{%d} to file %s\n', i, j, k, strcat('output/',output_file_name_base,'/',new_output_file_name));
                           output_file = fopen(strcat('output/',output_file_name_base,'/',new_output_file_name), 'w');
                           fprintf(output_file, getLayerTranslationString(i, W));
                           fprintf(output_file, rleCompressLayer(result, new_output_file_name));
                           cancer_layer_index = cancer_layer_index + 1;
                           fclose(output_file);
                       end
                   end

               end
           end

       end
    end
    
    %processLayersTranslation(layer_translation_file_name, strcat('output/',output_file_name_base));
    
    
end