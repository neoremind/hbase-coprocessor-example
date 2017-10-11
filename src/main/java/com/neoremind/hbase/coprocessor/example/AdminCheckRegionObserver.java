package com.neoremind.hbase.coprocessor.example;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.KeyValueUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * From HBase official example
 *
 * @author xu.zhang
 */
public class AdminCheckRegionObserver extends BaseRegionObserver {

    private static final byte[] ADMIN = Bytes.toBytes("admin");
    private static final byte[] COLUMN_FAMILY = Bytes.toBytes("details");
    private static final byte[] COLUMN = Bytes.toBytes("Admin_det");
    private static final byte[] VALUE = Bytes.toBytes("You can’t see Admin details");

    @Override
    public void start(CoprocessorEnvironment e) throws IOException {
        super.start(e);
        System.out.println("Start " + this.getClass().getName());
    }

    @Override
    public void preGetOp(final ObserverContext<RegionCoprocessorEnvironment> e,
                         final Get get, final List<Cell> results) throws IOException {
        if (Bytes.equals(get.getRow(), ADMIN)) {
            Cell c = CellUtil.createCell(get.getRow(), COLUMN_FAMILY, COLUMN, System.currentTimeMillis(),
                    (byte) 4, VALUE);
            results.add(c);
            e.bypass();
        }

        List<Cell> kvs = new ArrayList<>(results.size());
        for (Cell c : results) {
            kvs.add(KeyValueUtil.ensureKeyValue(c));
        }
        results.clear();
        results.addAll(kvs);
    }
}
