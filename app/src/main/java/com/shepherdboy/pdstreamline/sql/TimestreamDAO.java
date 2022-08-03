package com.shepherdboy.pdstreamline.sql;

import com.shepherdboy.pdstreamline.beans.Timestream;

public interface TimestreamDAO {

    Timestream getTimestream(String code);

}
