# Written by: Anusha Balakrishnan
#Date: 7/22/14
from datetime import datetime
import os
import time

date_format = None
def init():
    global date_format
    date_format = "%a %b %d %H:%M:%S %Y"

def get_simple_filename(filepath):
    pos = filepath.rfind('/')
    simple_filename = filepath[pos+1:]
    return simple_filename
def scrape_tweet_file(filepath, weekly_data_root, reset=False,
                      start_date = datetime.min, end_date = datetime.max):
    logfile = open("errors.log", 'a')
    simple_filename = get_simple_filename(filepath)
    if simple_filename.rfind('.') == -1:
        raise ValueError("Filename isn't in tweet.hashtag format.")
    hashtag = simple_filename[simple_filename.index('.')+1:]
    print "Hashtag: %s" % hashtag

    if not os.path.exists(weekly_data_root):
        os.makedirs(weekly_data_root)

    in_file = open(filepath, 'r')
    week_num = 0
    new_week = True
    out_file = None
    prev_date = datetime.min

    for line in in_file:
        line = line.strip("\n")
        fields = line.split('\t')
        timestamp = modify_timestamp(fields[1])
        try:
            curr_date = datetime.strptime(timestamp, date_format)

            # Ensure that date of tweet is within date range
            # If not, skip this tweet.
            if curr_date >= start_date and curr_date <= end_date:
                # handle case where Sunday of previous week sometimes follows days
                # of next week in data
                if curr_date < prev_date and prev_date.weekday() == 0:
                    # write tweet to previous week's file
                    dirname = weekly_data_root+"/Week_"+str(week_num-1)
                    temp_path = dirname+"/tweet."+hashtag
                    temp_out = open(temp_path, 'a')
                    temp_out.write(line+"\n")
                    # don't reset prev_date because that will create another new week
                    continue

                #other cases of bad ordering don't matter because they'll stay within the same week.
                if curr_date.weekday() == 0 and prev_date.weekday() == 6:
                    new_week = True

                if new_week:
                    # print "\tWeek end: %s" % (str(prev_date))
                    # print "Week start: %s" % (str(curr_date))
                    if not out_file == None:
                        out_file.close()

                    print "\tWeek %d completed.." %week_num

                    week_num+=1
                    dirname = weekly_data_root+"/Week_"+str(week_num)
                    if not os.path.exists(dirname):
                        os.makedirs(dirname)

                    out_path = dirname+"/tweet."+hashtag
                    out_file = open(out_path, 'a')
                    if reset:
                        out_file = open(out_path, 'w')
                    new_week = False

                out_file.write(line+"\n")
                prev_date = curr_date
            # else:
            #     print "Skipped tweet on %s" % str(curr_date)
        except ValueError:
            current_time = datetime.now()
            logfile.write("<"+current_time.strftime(date_format)+">\n")
            logfile.write("\t"+line+"\t["+filepath+"]\n")
            print "Wrote log to %s" % logfile

    in_file.close()
    logfile.close()
    out_file.close()

def modify_timestamp(timestamp):
    timestamp = timestamp.replace("EST ", "")
    timestamp = timestamp.replace("EDT ", "")
    return timestamp
def find_date_range(files, data_root):
    start_date = datetime.min
    end_date = datetime.max
    print "Calculating date range..."
    for filename in files:
        if not filename.startswith("tweet.") or ".date" in filename:
            continue
        in_file =  open(data_root+filename, 'r')
        cont = True
        while cont:
            first_line = in_file.readline().strip('\n')
            timestamp = modify_timestamp(first_line.split('\t')[1])
            try:
               conv = datetime.strptime(timestamp, date_format)
               if conv > start_date:
                   start_date= conv
               cont = False
            except ValueError:
                print "Error occurred. Trying next line."
        rev_lines = reversed(in_file.readlines())
        cont = True
        for last_line in rev_lines:
            if not cont:
                break
            timestamp = modify_timestamp(last_line.split('\t')[1])
            try:
                conv = datetime.strptime(timestamp, date_format)
                if conv < end_date:
                    end_date = conv
                cont = False
            except ValueError:
                print "Error occurred. Trying previous line."
        in_file.close()

    return datetime(start_date.year, start_date.month, start_date.day), \
           datetime(end_date.year, end_date.month, end_date.day)


def scrape_all_files(data_root, weekly_data, reset=False, constrained = False):
    if constrained:
        start, end = find_date_range(os.listdir(data_root), data_root)
        print "Start date: %s" % str(start)
        print "End date: %s" % str(end)
    else:
        start = datetime.min
        end = datetime.max
    for filename in os.listdir(data_root):
        if filename.startswith("tweet.") and ".date" not in filename:
            scrape_tweet_file(data_root+filename, weekly_data, reset, start, end)
init()
scrape_all_files('../twitter_data/', '../weekly_data_constrained', reset=True, constrained=True)
