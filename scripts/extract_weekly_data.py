# Written by: Anusha Balakrishnan
#Date: 7/22/14
from datetime import datetime
import os
import time

date_format = None
def init():
    """
    Initializes the global variable, date_format. ALWAYS call this function before using methods in this module.
    """
    global date_format
    # Date format for Twitter API timestamps, used by the datetime class to parse the timestamps
    date_format = "%a %b %d %H:%M:%S %Y"

def get_simple_filename(filepath):
    """
    Returns the simple filename for a given (absolute or relative) path. For example, given a file with
     the path "foo/bar/test", returns "test"
    :param filepath: The (absolute or relative) file path from which to obtain the simple filename (e.g.
    "foo/bar/test")
    :return: The simple filename (e.g. "test")
    """
    pos = filepath.rfind('/')
    simple_filename = filepath[pos+1:]
    return simple_filename

def scrape_tweet_file(filepath, weekly_data_root, reset=True,
                      start_date = datetime.min, end_date = datetime.max):
    """
    Scrapes a given file containing tweets and organizes the tweets by week, starting from Week 1, Week
    2, etc, where a week starts on Monday and ends the following Sunday. The organized tweets are written
    to files with the same name as the original file, and stored in a subdirectory of weekly_data_root
    that corresponds to the week number. For example, tweets from the file tweet.angry from Week 7 will
    be stored in weekly_data_root/Week_7/tweet.angry.
    If needed, the date range can be constrained by setting the start_date and end_date parameters. These
    default to the earliest time possible and the latest time possible respectively, but if set, only
    tweets within this date range (inclusive) are organized by week, and tweets outside of the range are
    ignored.
    NOTE: If calling this function to add to the previously extracted data for a given file for a week,
    set the reset parameter to False. Otherwise, data already in that file will be overwritten.
    :param filepath: The path for the file to be scraped.
    :param weekly_data_root: The root directory that the weekly data should be stored in.
    :param reset: (optional) Defaults to True. If set to False, then data for the existing file will not
    be overwritten.
    :param start_date: (optional) Defaults to the earliest past date possible. If set, tweets before this date
    will not be processed.
    :param end_date: (optional) Defaults to the latest future date possible. If set, tweets after this date will
    not be processed.
    :return: True if the function successfully executes, and also returns the path of the file to which logs are
    written. Logs are written in case certain tweets fail to be processed (which typically happens if the tweets
    are formatted incorrectly in the original data).
    """

    logfile = open("errors.log", 'a')
    simple_filename = get_simple_filename(filepath)
    if simple_filename.rfind('.') == -1:
        raise ValueError("Filename isn't in tweet.hashtag format.")
    hashtag = simple_filename[simple_filename.index('.')+1:]
    print "Hashtag: %s" % hashtag

    if not os.path.exists(weekly_data_root):
        # Create the directory to store weekly data in, if it doesn't already
        os.makedirs(weekly_data_root)

    in_file = open(filepath, 'r')
    week_num = 0
    new_week = True
    out_file = None
    prev_date = datetime.min #Keeps track of the date of the previous tweet, in order to
    # track the start of new weeks.

    for line in in_file:
        line = line.strip("\n")
        fields = line.split('\t')
        timestamp = modify_timestamp(fields[1])
        try:
            curr_date = datetime.strptime(timestamp, date_format)

            if curr_date >= start_date and curr_date <= end_date:
                # Ensures that date of tweet is within date range.
                # If not, skips this tweet.
                if curr_date < prev_date and prev_date.weekday() == 0:
                    # Handles case where Sunday of previous week sometimes follows Monday
                    # of next week in data.
                    # Writes tweet to previous week's file
                    dirname = weekly_data_root+"/Week_"+str(week_num-1)
                    temp_path = dirname+"/tweet."+hashtag
                    temp_out = open(temp_path, 'a')
                    temp_out.write(line+"\n")
                    # Don't reset prev_date because that will create another new week
                    continue


                if curr_date.weekday() == 0 and prev_date.weekday() == 6:
                    # If it's the first tweet on a Monday, set new_week to True
                    new_week = True

                if new_week:
                    if not out_file == None:
                        #Close the previous output file if it exists
                        out_file.close()

                    print "\tWeek %d completed.." %week_num

                    week_num+=1

                    dirname = weekly_data_root+"/Week_"+str(week_num)
                    if not os.path.exists(dirname):
                        # Create a new directory for the new week if it doesn't already exist
                        os.makedirs(dirname)

                    out_path = dirname+"/tweet."+hashtag
                    # By default, overwrite existing output file (if any) for new week
                    out_file = open(out_path, 'w')
                    if not reset:
                        # If adding to previously extracted data, append to existing output file (if any)
                        out_file = open(out_path, 'a')
                    new_week = False

                out_file.write(line+"\n")
                prev_date = curr_date
        except ValueError:
            current_time = datetime.now()
            logfile.write("<"+current_time.strftime(date_format)+">\n")
            logfile.write("\t"+line+"\t["+filepath+"]\n")
            print "Wrote log to %s" % logfile

    in_file.close()
    logfile.close()
    out_file.close()

    return (True, logfile.name)

def modify_timestamp(timestamp):
    """
    Modifies the tweet's timestamp to remove the time zone since it isn't supported in some versions of
     Python.
    NOTE: This ONLY removes GMT and abbreviations for time zones in the USA (EST, EDT, PST, PDT, etc.). If tweets
    are extracted from other time zones, code to replace them should be added below.
    :param timestamp: Timestamp of the tweet as logged by the Twitter API while extracting the tweets
    :return: Modified timestamp, without the time zone
    """
    timestamp = timestamp.replace("EST ", "")
    timestamp = timestamp.replace("EDT ", "")
    timestamp = timestamp.replace("PDT ", "")
    timestamp = timestamp.replace("PST ", "")
    timestamp = timestamp.replace("AST ", "")
    timestamp = timestamp.replace("AKST ", "")
    timestamp = timestamp.replace("AKDT ", "")
    timestamp = timestamp.replace("CST ", "")
    timestamp = timestamp.replace("CDT ", "")
    timestamp = timestamp.replace("MST ", "")
    timestamp = timestamp.replace("MDT ", "")
    timestamp = timestamp.replace("HST ", "")
    timestamp = timestamp.replace("HAST ", "")
    timestamp = timestamp.replace("HADT ", "")
    timestamp = timestamp.replace("SST ", "")
    timestamp = timestamp.replace("SDT ", "")
    timestamp = timestamp.replace("CHST ", "")
    timestamp = timestamp.replace("GMT ", "")
    return timestamp

def find_date_range(files, data_root):
    """
    Finds the largest date range common to the extracted tweets in a given set of files. This can be done in order to "align" the data, and ensure that when tweets are organized by week, each week contains tweets from all the files in the set, rather than just a few that had tweets extracted in that week. The date range returned by this function can then be used to constrain the tweets in scrape_tweet_file().
    :param files: All the files that are to be considered while finding the date range.
    :param data_root: The directory that all the files are present in.
    :return: The earliest start date and the latest end date common to all the tweets in the given set of files
    """
    start_date = datetime.min
    end_date = datetime.max
    for filename in files:
        if not filename.startswith("tweet.") or ".date" in filename:
            # Skip invalid file names
            continue
        in_file =  open(data_root+filename, 'r')
        cont = True
        while cont:
            # Search the first line(s) of each file to see the earliest date for tweets extracted from that file.
            first_line = in_file.readline().strip('\n')
            timestamp = modify_timestamp(first_line.split('\t')[1])
            try:
               # Attempt to parse the timestamp of the tweet, and compare it to the current start date for the
               # date range.
               # If this start date is later than the previous date, set this date as the new start date for the
               # date range.
               conv = datetime.strptime(timestamp, date_format)
               if conv > start_date:
                   start_date= conv

               cont = False
            except ValueError:
                # If the timestamp couldn't be parsed, try the next tweet.
                continue
        # Read the line in reverse to get the date of the last tweet
        rev_lines = reversed(in_file.readlines())
        cont = True
        for last_line in rev_lines:
            if not cont:
                break
            timestamp = modify_timestamp(last_line.split('\t')[1])
            try:
                # Attempt to parse the timestamp, and compare it to the current end date of the date range.
                # If it is earlier than the current end date, set this date as the new end date.
                conv = datetime.strptime(timestamp, date_format)
                if conv < end_date:
                    end_date = conv
                cont = False
            except ValueError:
                # If the timestamp couldn't be parsed, try the previous tweet.
                continue
        in_file.close()

    return datetime(start_date.year, start_date.month, start_date.day), \
           datetime(end_date.year, end_date.month, end_date.day)


def scrape_all_files(data_root, weekly_data):
    """
    Find the date range for all the data files, and organize all of them by week.
    :param data_root: The directory where all the unorganized data is stored.
    :param weekly_data: The directory where the weekly data should be stored.
    :return:
    """
    start, end = find_date_range(os.listdir(data_root), data_root)

    # print "Start date: %s" % str(start)
    # print "End date: %s" % str(end)

    for filename in os.listdir(data_root):
        if filename.startswith("tweet.") and ".date" not in filename:
            # Consider only valid filenames
            scrape_tweet_file(data_root+filename, weekly_data, start, end)
init()

# Use the function call below to organize all the data in ../twitter_data/ by week and store the
# weekly data in ../weekly_data_constrained

# scrape_all_files('../twitter_data/', '../weekly_data_constrained')
