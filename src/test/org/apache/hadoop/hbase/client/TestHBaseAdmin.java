begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseClusterTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HColumnDescriptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HTableDescriptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|HRegion
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
import|;
end_import

begin_class
specifier|public
class|class
name|TestHBaseAdmin
extends|extends
name|HBaseClusterTestCase
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestHBaseAdmin
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|String
name|TABLE_STR
init|=
literal|"testTable"
decl_stmt|;
specifier|private
name|byte
index|[]
name|TABLE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|TABLE_STR
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testRow"
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testFamily"
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testQualifier"
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
name|VALUE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testValue"
argument_list|)
decl_stmt|;
specifier|private
name|HBaseAdmin
name|admin
init|=
literal|null
decl_stmt|;
specifier|private
name|HConnection
name|connection
init|=
literal|null
decl_stmt|;
comment|/**    * Constructor does nothing special, start cluster.    */
specifier|public
name|TestHBaseAdmin
parameter_list|()
throws|throws
name|Exception
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testCreateTable
parameter_list|()
throws|throws
name|IOException
block|{
name|init
argument_list|()
expr_stmt|;
name|HTableDescriptor
index|[]
name|tables
init|=
name|connection
operator|.
name|listTables
argument_list|()
decl_stmt|;
name|int
name|numTables
init|=
name|tables
operator|.
name|length
decl_stmt|;
name|createTable
argument_list|(
name|TABLE
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|tables
operator|=
name|connection
operator|.
name|listTables
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|numTables
operator|+
literal|1
argument_list|,
name|tables
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testDisableAndEnableTable
parameter_list|()
throws|throws
name|IOException
block|{
name|init
argument_list|()
expr_stmt|;
name|HTable
name|ht
init|=
name|createTable
argument_list|(
name|TABLE
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|ht
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
comment|//Test that table is disabled
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
name|boolean
name|ok
init|=
literal|false
decl_stmt|;
try|try
block|{
name|ht
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RetriesExhaustedException
name|e
parameter_list|)
block|{
name|ok
operator|=
literal|true
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|ok
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
comment|//Test that table is enabled
try|try
block|{
name|ht
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RetriesExhaustedException
name|e
parameter_list|)
block|{
name|ok
operator|=
literal|false
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|ok
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testTableExist
parameter_list|()
throws|throws
name|IOException
block|{
name|init
argument_list|()
expr_stmt|;
name|boolean
name|exist
init|=
literal|false
decl_stmt|;
name|exist
operator|=
name|admin
operator|.
name|tableExists
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|exist
argument_list|)
expr_stmt|;
name|createTable
argument_list|(
name|TABLE
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|exist
operator|=
name|admin
operator|.
name|tableExists
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exist
argument_list|)
expr_stmt|;
block|}
comment|//  public void testMajorCompact() throws Exception {
comment|//    init();
comment|//
comment|//    int testTableCount = 0;
comment|//    int flushSleep = 1000;
comment|//    int majocCompactSleep = 7000;
comment|//
comment|//    HTable ht = createTable(TABLE, FAMILY);
comment|//    byte [][] ROWS = makeN(ROW, 5);
comment|//
comment|//    Put put = new Put(ROWS[0]);
comment|//    put.add(FAMILY, QUALIFIER, VALUE);
comment|//    ht.put(put);
comment|//
comment|//    admin.flush(TABLE);
comment|//    Thread.sleep(flushSleep);
comment|//
comment|//    put = new Put(ROWS[1]);
comment|//    put.add(FAMILY, QUALIFIER, VALUE);
comment|//    ht.put(put);
comment|//
comment|//    admin.flush(TABLE);
comment|//    Thread.sleep(flushSleep);
comment|//
comment|//    put = new Put(ROWS[2]);
comment|//    put.add(FAMILY, QUALIFIER, VALUE);
comment|//    ht.put(put);
comment|//
comment|//    admin.flush(TABLE);
comment|//    Thread.sleep(flushSleep);
comment|//
comment|//    put = new Put(ROWS[3]);
comment|//    put.add(FAMILY, QUALIFIER, VALUE);
comment|//    ht.put(put);
comment|//
comment|//    admin.majorCompact(TABLE);
comment|//    Thread.sleep(majocCompactSleep);
comment|//
comment|//    HRegion [] regions = null;
comment|//
comment|//    regions = connection.getRegionServerWithRetries(
comment|//        new ServerCallable<HRegion []>(connection, TABLE, ROW) {
comment|//          public HRegion [] call() throws IOException {
comment|//            return server.getOnlineRegionsAsArray();
comment|//          }
comment|//        }
comment|//    );
comment|//    for(HRegion region : regions) {
comment|//      String table = Bytes.toString(region.getRegionName()).split(",")[0];
comment|//      if(table.equals(TABLE_STR)) {
comment|//        String output = "table: " + table;
comment|//        int i = 0;
comment|//        for(int j : region.getStoresSize()) {
comment|//          output += ", files in store " + i++ + "(" + j + ")";
comment|//          testTableCount = j;
comment|//        }
comment|//        if (LOG.isDebugEnabled()) {
comment|//          LOG.debug(output);
comment|//        }
comment|//        System.out.println(output);
comment|//      }
comment|//    }
comment|//    assertEquals(1, testTableCount);
comment|//  }
comment|//
comment|//
comment|//
comment|//  public void testFlush_TableName() throws Exception {
comment|//    init();
comment|//
comment|//    int initTestTableCount = 0;
comment|//    int testTableCount = 0;
comment|//
comment|//    HTable ht = createTable(TABLE, FAMILY);
comment|//
comment|//    Put put = new Put(ROW);
comment|//    put.add(FAMILY, QUALIFIER, VALUE);
comment|//    ht.put(put);
comment|//
comment|//    HRegion [] regions = null;
comment|//
comment|//    regions = connection.getRegionServerWithRetries(
comment|//        new ServerCallable<HRegion []>(connection, TABLE, ROW) {
comment|//          public HRegion [] call() throws IOException {
comment|//            return server.getOnlineRegionsAsArray();
comment|//          }
comment|//        }
comment|//    );
comment|//    for(HRegion region : regions) {
comment|//      String table = Bytes.toString(region.getRegionName()).split(",")[0];
comment|//      if(table.equals(TABLE_STR)) {
comment|//        String output = "table: " + table;
comment|//        int i = 0;
comment|//        for(int j : region.getStoresSize()) {
comment|//          output += ", files in store " + i++ + "(" + j + ")";
comment|//          initTestTableCount = j;
comment|//        }
comment|//        if (LOG.isDebugEnabled()) {
comment|//          LOG.debug(output);
comment|//        }
comment|//      }
comment|//    }
comment|//
comment|//    //Flushing
comment|//    admin.flush(TABLE);
comment|//    Thread.sleep(2000);
comment|//
comment|//    regions = connection.getRegionServerWithRetries(
comment|//        new ServerCallable<HRegion []>(connection, TABLE, ROW) {
comment|//          public HRegion [] call() throws IOException {
comment|//            return server.getOnlineRegionsAsArray();
comment|//          }
comment|//        }
comment|//    );
comment|//    for(HRegion region : regions) {
comment|//      String table = Bytes.toString(region.getRegionName()).split(",")[0];
comment|//      if(table.equals(TABLE_STR)) {
comment|//        String output = "table: " + table;
comment|//        int i = 0;
comment|//        for(int j : region.getStoresSize()) {
comment|//          output += ", files in store " + i++ + "(" + j + ")";
comment|//          testTableCount = j;
comment|//        }
comment|//        if (LOG.isDebugEnabled()) {
comment|//          LOG.debug(output);
comment|//        }
comment|//      }
comment|//    }
comment|//
comment|//    assertEquals(initTestTableCount + 1, testTableCount);
comment|//  }
comment|//
comment|//
comment|//  public void testFlush_RegionName() throws Exception{
comment|//    init();
comment|//    int initTestTableCount = 0;
comment|//    int testTableCount = 0;
comment|//    String regionName = null;
comment|//
comment|//    HTable ht = createTable(TABLE, FAMILY);
comment|//
comment|//    Put put = new Put(ROW);
comment|//    put.add(FAMILY, QUALIFIER, VALUE);
comment|//    ht.put(put);
comment|//
comment|//    HRegion [] regions = null;
comment|//
comment|//    regions = connection.getRegionServerWithRetries(
comment|//        new ServerCallable<HRegion []>(connection, TABLE, ROW) {
comment|//          public HRegion [] call() throws IOException {
comment|//            return server.getOnlineRegionsAsArray();
comment|//          }
comment|//        }
comment|//    );
comment|//    for(HRegion region : regions) {
comment|//      String reg = Bytes.toString(region.getRegionName());
comment|//      String table = reg.split(",")[0];
comment|//      if(table.equals(TABLE_STR)) {
comment|//        regionName = reg;
comment|//        String output = "table: " + table;
comment|//        int i = 0;
comment|//        for(int j : region.getStoresSize()) {
comment|//          output += ", files in store " + i++ + "(" + j + ")";
comment|//          initTestTableCount = j;
comment|//        }
comment|//        if (LOG.isDebugEnabled()) {
comment|//          LOG.debug(output);
comment|//        }
comment|//      }
comment|//    }
comment|//
comment|//    //Flushing
comment|//    admin.flush(regionName);
comment|//    Thread.sleep(2000);
comment|//
comment|//    regions = connection.getRegionServerWithRetries(
comment|//        new ServerCallable<HRegion []>(connection, TABLE, ROW) {
comment|//          public HRegion [] call() throws IOException {
comment|//            return server.getOnlineRegionsAsArray();
comment|//          }
comment|//        }
comment|//    );
comment|//    for(HRegion region : regions) {
comment|//      String table = Bytes.toString(region.getRegionName()).split(",")[0];
comment|//      if(table.equals(TABLE_STR)) {
comment|//        String output = "table: " + table;
comment|//        int i = 0;
comment|//        for(int j : region.getStoresSize()) {
comment|//          output += ", files in store " + i++ + "(" + j + ")";
comment|//          testTableCount = j;
comment|//        }
comment|//        if (LOG.isDebugEnabled()) {
comment|//          LOG.debug(output);
comment|//        }
comment|//      }
comment|//    }
comment|//
comment|//    assertEquals(initTestTableCount + 1, testTableCount);
comment|//  }
comment|//////////////////////////////////////////////////////////////////////////////
comment|// Helpers
comment|//////////////////////////////////////////////////////////////////////////////
specifier|private
name|byte
index|[]
index|[]
name|makeN
parameter_list|(
name|byte
index|[]
name|base
parameter_list|,
name|int
name|n
parameter_list|)
block|{
name|byte
index|[]
index|[]
name|ret
init|=
operator|new
name|byte
index|[
name|n
index|]
index|[]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|ret
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|add
argument_list|(
name|base
argument_list|,
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
name|i
block|}
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|private
name|HTable
name|createTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|admin
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
return|return
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
return|;
block|}
specifier|private
name|void
name|init
parameter_list|()
throws|throws
name|IOException
block|{
name|connection
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
operator|.
name|connection
expr_stmt|;
name|admin
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

