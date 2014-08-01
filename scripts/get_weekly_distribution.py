# Written by: Anusha Balakrishnan
#Date: 7/22/14
import os

def write_all_distributions(weekly_data_root, output = None):
    """
    Calculates the weekly distribution of tweets for each emotion hashtag. To do this, it scans the
     directory containing tweets, organized by week and emotion, and writes the final weekly distribution
     to a single file.
    :param weekly_data_root: The directory that the weekly data is stored in. This directory must contain
    one subdirectory corresponding to each week of the data, where each such subdirectory must follow the
    same naming convention as the directories created by extract_weekly_data (Week_1, Week_2, etc.) If
    any other naming conventions are followed, ths function will raise an error.
        Similarly, each file's name within these subdirectories must follow the format 'tweet.EMOTION',
    where EMOTION is the hashtag that all of the tweets in that file contain. Any files not following
    this convention will be skipped.
    :param output: (optional) The path for the output file. The distribution of by hashtag will be stored
    as tab-separated values in this file.
        If unspecified, the output file is saved as hashtag_distribution in the directory specified
         by weekly_data_root.
    """
    if weekly_data_root[-1]!='/':
        weekly_data_root = weekly_data_root + '/'
    # Default output file is hashtag_distribution.tsv
    if not output:
        output = "%shashtag_distribution.tsv" % weekly_data_root

    freqs = {} # Dictionary to map each hashtag to its frequency per week
    names = [] # Stores all the hashtags to write to the header of the output file
    num_weeks = 0

    for dirname in os.listdir(weekly_data_root):
        num_weeks += 1
        if '_' not in dirname:
            raise ValueError("All directories with weekly data must be named Week_X, where X is the week number.")
        week_ind = int(dirname[dirname.index('_')+1:]) - 1
        for filename in os.listdir(weekly_data_root+dirname):

            if '.' not in filename:
                continue
            hashtag = filename[filename.index('.')+1:]
            if hashtag not in names:
                names.append(hashtag)

            read_file = open(weekly_data_root+dirname+'/'+filename)

            # The number of lines in the file is the number of tweets for that week
            length = len(read_file.readlines())

            if hashtag not in freqs.keys():
                freqs[hashtag] = [None]*18

            freqs[hashtag][week_ind] = length #Save the frequency for the current week and current hashtag

            # out_file.write("%s\t%s\t%d\n" % (week_num, filename, length))
            read_file.close()

    out_file = open(output, 'w')
    out_file.write('\t'.join(names)+"\n") # Writes names of all hashtags to file header
    for week in range(0, num_weeks):
        # For each week in the data, write the frequencies for all the hashtags to the output file
        line = ''
        for n in names:
            line = line + str(freqs[n][week]) + '\t'
        line = line.strip('\t')
        out_file.write(line+"\n")
    out_file.close()

def write_sentiment_distribution(weekly_data_root, output = None):
    """
    Calculates the weekly distribution of tweets for positive sentiment, negative sentiment, and sarcasm.
    To do this, it scans the directory containing tweets, organized by week and emotion, and writes the
    final weekly distribution to a single file. The sentiment for each file is determined by its hashtag.
    :param weekly_data_root: The directory that the weekly data is stored in. This directory must contain
    directories for each week, where each such subdirectory must follow the same naming convention as the
    directories created by extract_weekly_data (Week_1, Week_2, etc.) If any other naming convention is
    followed, ths function will raise an error.
        Similarly, each file's name within these subdirectories must follow the format 'tweet.EMOTION',
    where EMOTION is the hashtag that all of the tweets in that file contain. Any files not following
    this convention will be skipped.
    :param output: (optional) The path for the output file. The distribution of tweets by sentiment will
     be stored as tab-separated values in this file.
        If unspecified, the output file is saved as sentiment_distribution in the directory specified
        by weekly_data_root.
    """

    # Mapping of each hashtag to positive sentiment or negative sentiment. Any hashtags not in these lists are considered sarcasm.
    pos_sent = ["excited", "happy", "grateful", "joy", "love", "loved", "lucky", "wonderful"]
    neg_sent = ["angry", "awful", "disappointed", "fear", "frustrated", "hate", "sad", "scared",
                "stressed"]

    if weekly_data_root[-1]!='/':
        weekly_data_root = weekly_data_root + '/'
    # Default output file is sentiment_distribution.tsv
    if not output:
        output = "%ssentiment_distribution.tsv" % weekly_data_root
    freqs = {"sarcasm":[0]*18, "pos":[0]*18, "neg":[0]*18}
    num_weeks = 0

    for dirname in os.listdir(weekly_data_root):
        num_weeks += 1
        week_ind = int(dirname[dirname.index('_')+1:]) - 1
        for filename in os.listdir(weekly_data_root+dirname):
            hashtag = filename[filename.index('.')+1:]
            if '.' not in hashtag:
                continue

            read_file = open(weekly_data_root+dirname+'/'+filename)
            length = len(read_file.readlines())

            # Determine the sentiment of the current hashtag
            sent = "sarcasm"
            if hashtag in pos_sent:
                sent = "pos"
            elif hashtag in neg_sent:
                sent = "neg"

            #Add the current hashtag's frequency to the frequency of the corresponding sentiment for that
            # week
            freqs[sent][week_ind] += length
            read_file.close()


    out_file = open(output, 'w')
    # Write "positive", "negative", and "sarcasm" to the header of the file.
    out_file.write('\t'.join(freqs.keys())+"\n")
    for week in range(0, num_weeks):
        # For each week and for each sentiment, write its frequency to the output file.
        line = ''
        for n in freqs.keys():

            line = line + str(freqs[n][week]) + '\t'
        line = line.strip('\t')
        out_file.write(line+"\n")
        # print line
    out_file.close()

# Call this function to extract the weekly tweet distribution, by hashtag, for weekly data stored in ../weekly_data_constrained, and store the distribution in hashtag_distribution.tsv
# write_all_distributions("../weekly_data_constrained/", "hashtag_distribution.tsv")

# Call this function to extract the weekly tweet distribution, by sentiment, for weekly data stored in ../weekly_data_constrained, and store the distribution in sentiment_distribution.tsv
# write_sentiment_distribution("../weekly_data_constrained/", "sentiment_distribution.tsv")