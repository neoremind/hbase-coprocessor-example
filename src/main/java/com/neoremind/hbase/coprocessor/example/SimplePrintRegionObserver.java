package com.neoremind.hbase.coprocessor.example;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.KeyValueUtil;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

/**
 * Print out all cells
 *
 * @author xu.zhang
 */
public class SimplePrintRegionObserver extends BaseRegionObserver {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void start(CoprocessorEnvironment e) throws IOException {
        super.start(e);
        logger.info("Start " + this.getClass().getName());
    }

    @Override
    public void postPut(ObserverContext<RegionCoprocessorEnvironment> e, Put put, WALEdit edit, Durability
            durability) throws IOException {
        NavigableMap<byte[], List<Cell>> cells = put.getFamilyCellMap();
        for (Map.Entry<byte[], List<Cell>> entry : cells.entrySet()) {
            List<Cell> cellList = entry.getValue();
            showCell(cellList);
        }
        e.bypass();
    }

    public void showCell(List<Cell> cellList) {
        for (Cell cell : cellList) {
            logger.info("RowKey={}, Column={}.{}, TS={}, Value={}", new String(CellUtil.cloneRow(cell)),
                    new String(CellUtil.cloneFamily(cell)),
                    new String(CellUtil.cloneQualifier(cell)),
                    cell.getTimestamp(),
                    new String(CellUtil.cloneValue(cell)));
        }
    }
}
