% przykladowa konwersja z data/ct7b.mat -> data/output/ct7b_typ_x-y.json, gdzie
% typ -> jaki obiekt przedstawia mapa - nerka / cancer
% x -> numer warstwy
% y -> numer skanu dla danego typu i warstwy

function convertCT(input_file_name)
    fprintf('Processing file: %s\n', input_file_name); 
    output_file_name_base = extractBefore(input_file_name, '.mat');

    load(input_file_name);
    result = {};

    for i = 1:length(R)
       if ~isempty(R{i})
           kidney_layer_index = 0;
           cancer_layer_index = 0;
           
           for j = 1:length(R{i})
               if ~isempty(R{i}{j})
                   for k=1:(length(R{i}{j})-1)
                       % nerka
                       if ~isempty(R{i}{j}{k}) && strcmp(R{i}{j}{k+1},'nerka')
                           result = R{i}{j}{k};
                           new_output_file_name = sprintf('%s_kidney_%d-%d.ctl', output_file_name_base, i, kidney_layer_index); 
                           fprintf('Adding R{%d}{%d}{%d} to file %s\n', i, j, k, strcat('output/',new_output_file_name));
                           output_file = fopen(strcat('output/',new_output_file_name), 'w');
                           fprintf(output_file, rleCompressLayer(result, new_output_file_name));
                           kidney_layer_index = kidney_layer_index + 1;
                       end
                       
                       % rak
                       if ~isempty(R{i}{j}{k}) && strcmp(R{i}{j}{k+1},'Cancer')
                           result = R{i}{j}{k};
                           new_output_file_name = sprintf('%s_cancer_%d-%d.ctl', output_file_name_base, i, cancer_layer_index); 
                           fprintf('Adding R{%d}{%d}{%d} to file %s\n', i, j, k, strcat('output/',new_output_file_name));
                           output_file = fopen(strcat('output/',new_output_file_name), 'w');
                           fprintf(output_file, rleCompressLayer(result, new_output_file_name));
                           cancer_layer_index = cancer_layer_index + 1;
                       end
                   end
                   
               end
           end
           
       end    
    end
end