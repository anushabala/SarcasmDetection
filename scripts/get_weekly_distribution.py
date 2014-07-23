# Written by: Anusha Balakrishnan
#Date: 7/22/14
import os


def write_distribution(weekly_data_root, output):
    if weekly_data_root[-1]!='/':
        weekly_data_root = weekly_data_root + '/'

    freqs = {}
    names = []
    num_weeks = 0
    for dirname in os.listdir(weekly_data_root):
        num_weeks += 1
        for filename in os.listdir(weekly_data_root+dirname):
            hashtag = filename[filename.index('.')+1:]
            if hashtag not in names:
                names.append(hashtag)

            read_file = open(weekly_data_root+dirname+'/'+filename)
            length = len(read_file.readlines())
            if hashtag not in freqs.keys():
                freqs[hashtag] = []
            freqs[hashtag].append(length)
            # week_num = dirname[dirname.index('_')+1:]
            # out_file.write("%s\t%s\t%d\n" % (week_num, filename, length))
            read_file.close()

    out_file = open(output, 'w')
    out_file.write('\t'.join(names)+"\n")
    # print '\t'.join(names)
    for week in range(0, num_weeks):
        line = ''
        for n in names:
            line = line + str(freqs[n][week]) + '\t'
        line = line.strip('\t')
        out_file.write(line+"\n")
        # print line
    out_file.close()

def write_ternary_distribution(weekly_data_root, output):
    pos_sent = ["excited", "happy", "grateful", "joy", "love", "loved", "lucky", "wonderful"]
    neg_sent = ["angry", "awful", "disappointed", "fear", "frustrated", "hate", "sad", "scared", "stressed"]
write_distribution("../weekly_data_constrained/", "distribution.tsv")