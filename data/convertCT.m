input_file_name = 'ct7b.mat';
output_file_name_base = extractBefore(input_file_name, '.mat');

load(input_file_name);
result = {};
result_index = 0;

for i = 1:length(R)
   if ~isempty(R{i})
       for j = 1:length(R{i})
           if ~isempty(R{i}{j})
               for k=1:(length(R{i}{j})-1)
                   if ~isempty(R{i}{j}{k}) && strcmp(R{i}{j}{k+1},'nerka')
                       result = R{i}{j}{k};
                       new_output_file_name = sprintf('%s_%d.json', output_file_name_base, result_index); 
                       fprintf('Adding R{%d}{%d}{%d} to file %s\n', i, j, k, strcat('output/',new_output_file_name));
                       output_file = fopen(strcat('output/',new_output_file_name), 'w');
                       fprintf(output_file, jsonencode(result));
                       
                       result_index = result_index + 1;
                   end
               end
           end
       end
   end    
end    