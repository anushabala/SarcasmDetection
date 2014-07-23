# Written by: Anusha Balakrishnan
#Date: 7/22/14
import os


def write_distribution(weekly_data_root, output):
    out_file = open(output, 'w')
    if weekly_data_root[-1]!='/':
        weekly_data_root = weekly_data_root + '/'

    for dirname in os.listdir(weekly_data_root):
        for filename in os.listdir(weekly_data_root+dirname):
            read_file = open(weekly_data_root+dirname+'/'+filename)
            length = len(read_file.readlines())
            week_num = dirname[dirname.index('_')+1:]
            out_file.write("%s\t%s\t%d\n" % (week_num, filename, length))
            read_file.close()
    out_file.close()

write_distribution("../weekly_data_constrained/", "distribution.out")