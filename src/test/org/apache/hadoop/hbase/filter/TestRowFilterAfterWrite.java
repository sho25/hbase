begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

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
name|filter
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
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|HConstants
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
name|HStoreKey
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
name|client
operator|.
name|HBaseAdmin
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
name|client
operator|.
name|HTable
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
name|client
operator|.
name|Scanner
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
name|io
operator|.
name|BatchUpdate
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
name|io
operator|.
name|RowResult
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
name|io
operator|.
name|Cell
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/** Test regexp filters HBASE-476 */
end_comment

begin_class
specifier|public
class|class
name|TestRowFilterAfterWrite
extends|extends
name|HBaseClusterTestCase
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"hiding"
argument_list|)
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestRowFilterAfterWrite
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"TestTable"
decl_stmt|;
specifier|static
specifier|final
name|String
name|FAMILY
init|=
literal|"C:"
decl_stmt|;
specifier|static
specifier|final
name|String
name|COLUMN1
init|=
name|FAMILY
operator|+
literal|"col1"
decl_stmt|;
specifier|static
specifier|final
name|Text
name|TEXT_COLUMN1
init|=
operator|new
name|Text
argument_list|(
name|COLUMN1
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|COLUMN2
init|=
name|FAMILY
operator|+
literal|"col2"
decl_stmt|;
specifier|static
specifier|final
name|Text
name|TEXT_COLUMN2
init|=
operator|new
name|Text
argument_list|(
name|COLUMN2
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
index|[]
name|columns
init|=
block|{
name|TEXT_COLUMN1
block|,
name|TEXT_COLUMN2
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_ROWS
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|VALUE_SIZE
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VALUE
init|=
operator|new
name|byte
index|[
name|VALUE_SIZE
index|]
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|COL_2_SIZE
init|=
literal|5
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|KEY_SIZE
init|=
literal|9
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_REWRITES
init|=
literal|10
decl_stmt|;
static|static
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|VALUE
argument_list|,
operator|(
name|byte
operator|)
literal|'a'
argument_list|)
expr_stmt|;
block|}
comment|/** constructor */
specifier|public
name|TestRowFilterAfterWrite
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// Make sure the cache gets flushed so we get multiple stores
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hregion.memcache.flush.size"
argument_list|,
operator|(
name|NUM_ROWS
operator|*
operator|(
name|VALUE_SIZE
operator|+
name|COL_2_SIZE
operator|+
name|KEY_SIZE
operator|)
operator|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"memcach flush : "
operator|+
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.hregion.memcache.flush.size"
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.optionalcacheflushinterval"
argument_list|,
literal|100000000
argument_list|)
expr_stmt|;
comment|// Avoid compaction to keep multiple stores
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
comment|// Make lease timeout longer, lease checks less frequent
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.lease.period"
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.lease.thread.wakefrequency"
argument_list|,
literal|5
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// For debugging
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.lease.period"
argument_list|,
literal|20
operator|*
literal|60
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"ipc.client.timeout"
argument_list|,
literal|20
operator|*
literal|60
operator|*
literal|1000
argument_list|)
expr_stmt|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test hbase mapreduce jobs against single region and multi-region tables.    *     * @throws IOException    * @throws InterruptedException    */
specifier|public
name|void
name|testAfterWrite
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|singleTableTest
argument_list|()
expr_stmt|;
block|}
comment|/*    * Test against a single region. @throws IOException    */
specifier|private
name|void
name|singleTableTest
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
comment|// Create a table.
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
comment|// insert some data into the test table
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|)
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
name|NUM_ROWS
condition|;
name|i
operator|++
control|)
block|{
name|BatchUpdate
name|b
init|=
operator|new
name|BatchUpdate
argument_list|(
operator|new
name|Text
argument_list|(
literal|"row_"
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%1$05d"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|b
operator|.
name|put
argument_list|(
name|TEXT_COLUMN1
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|b
operator|.
name|put
argument_list|(
name|TEXT_COLUMN2
argument_list|,
name|String
operator|.
name|format
argument_list|(
literal|"%1$05d"
argument_list|,
name|i
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|commit
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
comment|// LOG.info("Print table contents using scanner before map/reduce for " + TABLE_NAME);
comment|// scanTable(TABLE_NAME, false);
comment|// LOG.info("Print table contents using scanner+filter before map/reduce for " + TABLE_NAME);
comment|// scanTableWithRowFilter(TABLE_NAME, false);
comment|// Do some identity write operations on one column of the data.
for|for
control|(
name|int
name|n
init|=
literal|0
init|;
name|n
operator|<
name|NUM_REWRITES
condition|;
name|n
operator|++
control|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUM_ROWS
condition|;
name|i
operator|++
control|)
block|{
name|BatchUpdate
name|b
init|=
operator|new
name|BatchUpdate
argument_list|(
operator|new
name|Text
argument_list|(
literal|"row_"
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%1$05d"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|b
operator|.
name|put
argument_list|(
name|TEXT_COLUMN2
argument_list|,
name|String
operator|.
name|format
argument_list|(
literal|"%1$05d"
argument_list|,
name|i
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|commit
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Wait for the flush to happen
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting, for flushes to complete"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|5
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// Wait for the flush to happen
name|LOG
operator|.
name|info
argument_list|(
literal|"Done. No flush should happen after this"
argument_list|)
expr_stmt|;
comment|// Do another round so to populate the mem cache
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUM_ROWS
condition|;
name|i
operator|++
control|)
block|{
name|BatchUpdate
name|b
init|=
operator|new
name|BatchUpdate
argument_list|(
operator|new
name|Text
argument_list|(
literal|"row_"
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%1$05d"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|b
operator|.
name|put
argument_list|(
name|TEXT_COLUMN2
argument_list|,
name|String
operator|.
name|format
argument_list|(
literal|"%1$05d"
argument_list|,
name|i
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|commit
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Print table contents using scanner after map/reduce for "
operator|+
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|scanTable
argument_list|(
name|TABLE_NAME
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Print table contents using scanner+filter after map/reduce for "
operator|+
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|scanTableWithRowFilter
argument_list|(
name|TABLE_NAME
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|scanTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|,
specifier|final
name|boolean
name|printValues
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|Scanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|columns
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
name|int
name|numFound
init|=
name|doScan
argument_list|(
name|scanner
argument_list|,
name|printValues
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|NUM_ROWS
argument_list|,
name|numFound
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|scanTableWithRowFilter
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|,
specifier|final
name|boolean
name|printValues
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|columnMap
init|=
operator|new
name|HashMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|columnMap
operator|.
name|put
argument_list|(
name|TEXT_COLUMN1
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|RegExpRowFilter
name|filter
init|=
operator|new
name|RegExpRowFilter
argument_list|(
literal|null
argument_list|,
name|columnMap
argument_list|)
decl_stmt|;
name|Scanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|columns
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|filter
argument_list|)
decl_stmt|;
name|int
name|numFound
init|=
name|doScan
argument_list|(
name|scanner
argument_list|,
name|printValues
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|NUM_ROWS
argument_list|,
name|numFound
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|doScan
parameter_list|(
specifier|final
name|Scanner
name|scanner
parameter_list|,
specifier|final
name|boolean
name|printValues
parameter_list|)
throws|throws
name|IOException
block|{
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
try|try
block|{
for|for
control|(
name|RowResult
name|result
range|:
name|scanner
control|)
block|{
if|if
condition|(
name|printValues
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"row: "
operator|+
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Text
argument_list|,
name|Cell
argument_list|>
name|e
range|:
name|result
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|" column: "
operator|+
name|e
operator|.
name|getKey
argument_list|()
operator|+
literal|" value: "
operator|+
operator|new
name|String
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|count
operator|++
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|count
return|;
block|}
block|}
block|}
end_class

end_unit

