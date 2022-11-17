import requests
import sys
from datetime import datetime
import time

now_unix = str(int(time.time()))
basepath = sys.path[0]
symbolfilenames = b = ["NYSE.txt", "NASDAQ.txt", "AMEX.txt"]


start_date_file = open(basepath + "/END_DATE_UNIX.txt", "r")
start_date_unix = start_date_file.readline()
start_date_file.close()

fullpath = basepath + "/data/test.html"
print("fullpath is: ", fullpath)

today = datetime.today().strftime('%Y-%m-%d')
for symbolfilename in symbolfilenames:
    print("symbolfilename ", symbolfilename)

    symbolfile = open( basepath + "/symbols/" + symbolfilename , "r")

    for aline in symbolfile.readlines():
        values = aline.split("\t")
        symbol = values[0]
    
        # url = 'https://query1.finance.yahoo.com/v7/finance/download/BABA?period1=1430959807&period2=1588879605&interval=1d&events=history'
        # url = 'https://query1.finance.yahoo.com/v7/finance/download/BABA?period1=1430956800&period2=1588809600&interval=1d&events=history'
        url = 'https://query1.finance.yahoo.com/v7/finance/download/' + symbol + '?period1=' + start_date_unix + '&period2=' + now_unix + '&interval=1d&events=history'
        myfile = requests.get(url, allow_redirects=True)
        fullpath = basepath + "/data/" + symbol + "_" + today + ".txt"
        open(fullpath, 'wb').write(myfile.content)

    symbolfile.close()

end_date_file = open(basepath + "/END_DATE_UNIX.txt", "w")
end_date_file.write(now_unix)
end_date_file.close()
