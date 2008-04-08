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
name|hadoop
operator|.
name|io
operator|.
name|Text
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
name|regionserver
operator|.
name|HRegionServer
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

begin_comment
comment|/**  * Test setting the global memcache size for a region server. When it reaches   * this size, any puts should be blocked while one or more forced flushes occurs  * to bring the memcache size back down.   */
end_comment

begin_class
specifier|public
class|class
name|TestGlobalMemcacheLimit
extends|extends
name|HBaseClusterTestCase
block|{
specifier|final
name|byte
index|[]
name|ONE_KB
init|=
operator|new
name|byte
index|[
literal|1024
index|]
decl_stmt|;
name|HTable
name|table1
decl_stmt|;
name|HTable
name|table2
decl_stmt|;
name|HRegionServer
name|server
decl_stmt|;
name|long
name|keySize
init|=
operator|(
operator|new
name|Text
argument_list|(
name|COLFAMILY_NAME1
argument_list|)
operator|)
operator|.
name|getLength
argument_list|()
operator|+
literal|9
operator|+
literal|8
decl_stmt|;
name|long
name|rowSize
init|=
name|keySize
operator|+
name|ONE_KB
operator|.
name|length
decl_stmt|;
comment|/**    * Get our hands into the cluster configuration before the hbase cluster     * starts up.    */
annotation|@
name|Override
specifier|public
name|void
name|preHBaseClusterSetup
parameter_list|()
block|{
comment|// we'll use a 2MB global memcache for testing's sake.
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.globalMemcacheLimit"
argument_list|,
literal|2
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
comment|// low memcache mark will be 1MB
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.globalMemcacheLimitLowMark"
argument_list|,
literal|1
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
comment|// make sure we don't do any optional flushes and confuse my tests.
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.optionalcacheflushinterval"
argument_list|,
literal|120000
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a table that we'll use to test.    */
annotation|@
name|Override
specifier|public
name|void
name|postHBaseClusterSetup
parameter_list|()
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|desc1
init|=
name|createTableDescriptor
argument_list|(
literal|"testTable1"
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|desc2
init|=
name|createTableDescriptor
argument_list|(
literal|"testTable2"
argument_list|)
decl_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc1
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc2
argument_list|)
expr_stmt|;
name|table1
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
literal|"testTable1"
argument_list|)
argument_list|)
expr_stmt|;
name|table2
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
literal|"testTable2"
argument_list|)
argument_list|)
expr_stmt|;
name|server
operator|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// there is a META region in play, and those are probably still in
comment|// the memcache for ROOT. flush it out.
for|for
control|(
name|HRegion
name|region
range|:
name|server
operator|.
name|getOnlineRegions
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
comment|// make sure we're starting at 0 so that it's easy to predict what the
comment|// results of our tests should be.
name|assertEquals
argument_list|(
literal|"Starting memcache size"
argument_list|,
literal|0
argument_list|,
name|server
operator|.
name|getGlobalMemcacheSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Make sure that region server thinks all the memcaches are as big as we were    * hoping they would be.    */
specifier|public
name|void
name|testMemcacheSizeAccounting
parameter_list|()
throws|throws
name|IOException
block|{
comment|// put some data in each of the two tables
name|long
name|dataSize
init|=
name|populate
argument_list|(
name|table1
argument_list|,
literal|500
argument_list|,
literal|0
argument_list|)
operator|+
name|populate
argument_list|(
name|table2
argument_list|,
literal|500
argument_list|,
literal|0
argument_list|)
decl_stmt|;
comment|// make sure the region server says it is using as much memory as we think
comment|// it is.
name|assertEquals
argument_list|(
literal|"Global memcache size"
argument_list|,
name|dataSize
argument_list|,
name|server
operator|.
name|getGlobalMemcacheSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that a put gets blocked and a flush is forced as expected when we     * reach the memcache size limit.    */
specifier|public
name|void
name|testBlocksAndForcesFlush
parameter_list|()
throws|throws
name|IOException
block|{
comment|// put some data in each of the two tables
name|long
name|startingDataSize
init|=
name|populate
argument_list|(
name|table1
argument_list|,
literal|500
argument_list|,
literal|0
argument_list|)
operator|+
name|populate
argument_list|(
name|table2
argument_list|,
literal|500
argument_list|,
literal|0
argument_list|)
decl_stmt|;
comment|// at this point we have 1052000 bytes in memcache. now, we'll keep adding
comment|// data to one of the tables until just before the global memcache limit,
comment|// noting that the globalMemcacheSize keeps growing as expected. then, we'll
comment|// do another put, causing it to go over the limit. when we look at the
comment|// globablMemcacheSize now, it should be<= the low limit.
name|long
name|dataNeeded
init|=
operator|(
literal|2
operator|*
literal|1024
operator|*
literal|1024
operator|)
operator|-
name|startingDataSize
decl_stmt|;
name|double
name|numRows
init|=
operator|(
name|double
operator|)
name|dataNeeded
operator|/
operator|(
name|double
operator|)
name|rowSize
decl_stmt|;
name|int
name|preFlushRows
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|floor
argument_list|(
name|numRows
argument_list|)
decl_stmt|;
name|long
name|dataAdded
init|=
name|populate
argument_list|(
name|table1
argument_list|,
name|preFlushRows
argument_list|,
literal|500
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Expected memcache size"
argument_list|,
name|dataAdded
operator|+
name|startingDataSize
argument_list|,
name|server
operator|.
name|getGlobalMemcacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|populate
argument_list|(
name|table1
argument_list|,
literal|2
argument_list|,
name|preFlushRows
operator|+
literal|500
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Post-flush memcache size"
argument_list|,
name|server
operator|.
name|getGlobalMemcacheSize
argument_list|()
operator|<=
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
block|}
specifier|private
name|long
name|populate
parameter_list|(
name|HTable
name|table
parameter_list|,
name|int
name|numRows
parameter_list|,
name|int
name|startKey
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|total
init|=
literal|0
decl_stmt|;
name|BatchUpdate
name|batchUpdate
init|=
literal|null
decl_stmt|;
name|Text
name|column
init|=
operator|new
name|Text
argument_list|(
name|COLFAMILY_NAME1
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|startKey
init|;
name|i
operator|<
name|startKey
operator|+
name|numRows
condition|;
name|i
operator|++
control|)
block|{
name|Text
name|key
init|=
operator|new
name|Text
argument_list|(
literal|"row_"
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%1$5d"
argument_list|,
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|total
operator|+=
name|key
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|total
operator|+=
name|column
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|total
operator|+=
literal|8
expr_stmt|;
name|total
operator|+=
name|ONE_KB
operator|.
name|length
expr_stmt|;
name|batchUpdate
operator|=
operator|new
name|BatchUpdate
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|batchUpdate
operator|.
name|put
argument_list|(
name|column
argument_list|,
name|ONE_KB
argument_list|)
expr_stmt|;
name|table
operator|.
name|commit
argument_list|(
name|batchUpdate
argument_list|)
expr_stmt|;
block|}
return|return
name|total
return|;
block|}
block|}
end_class

end_unit

