begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|catalog
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|conf
operator|.
name|Configuration
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
name|Abortable
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
name|HBaseTestingUtility
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
name|HRegionInfo
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
name|MediumTests
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
name|ServerName
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
name|util
operator|.
name|Bytes
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
name|Pair
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_comment
comment|/**  * Test {@link MetaReader}, {@link MetaEditor}.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestMetaReaderEditor
block|{
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
name|TestMetaReaderEditor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|ZooKeeperWatcher
name|zkw
decl_stmt|;
specifier|private
specifier|static
name|CatalogTracker
name|CT
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|Abortable
name|ABORTABLE
init|=
operator|new
name|Abortable
argument_list|()
block|{
specifier|private
specifier|final
name|AtomicBoolean
name|abort
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|why
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|abort
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
name|abort
operator|.
name|get
argument_list|()
return|;
block|}
block|}
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|Configuration
name|c
init|=
operator|new
name|Configuration
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// Tests to 4 retries every 5 seconds. Make it try every 1 second so more
comment|// responsive.  1 second is default as is ten retries.
name|c
operator|.
name|setLong
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|c
operator|.
name|setInt
argument_list|(
literal|"hbase.client.retries.number"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|zkw
operator|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|c
argument_list|,
literal|"TestMetaReaderEditor"
argument_list|,
name|ABORTABLE
argument_list|)
expr_stmt|;
name|CT
operator|=
operator|new
name|CatalogTracker
argument_list|(
name|zkw
argument_list|,
name|c
argument_list|,
name|ABORTABLE
argument_list|)
expr_stmt|;
name|CT
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|ABORTABLE
operator|.
name|abort
argument_list|(
literal|"test ending"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|CT
operator|.
name|stop
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Does {@link MetaReader#getRegion(CatalogTracker, byte[])} and a write    * against .META. while its hosted server is restarted to prove our retrying    * works.    * @throws IOException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testRetrying
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
specifier|final
name|String
name|name
init|=
literal|"testRetrying"
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started "
operator|+
name|name
argument_list|)
expr_stmt|;
specifier|final
name|byte
index|[]
name|nameBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|HTable
name|t
init|=
name|UTIL
operator|.
name|createTable
argument_list|(
name|nameBytes
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
decl_stmt|;
name|int
name|regionCount
init|=
name|UTIL
operator|.
name|createMultiRegions
argument_list|(
name|t
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
decl_stmt|;
comment|// Test it works getting a region from just made user table.
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|testGettingTableRegions
argument_list|(
name|CT
argument_list|,
name|nameBytes
argument_list|,
name|regionCount
argument_list|)
decl_stmt|;
name|MetaTask
name|reader
init|=
operator|new
name|MetaTask
argument_list|(
name|CT
argument_list|,
literal|"reader"
argument_list|)
block|{
annotation|@
name|Override
name|void
name|metaTask
parameter_list|()
throws|throws
name|Throwable
block|{
name|testGetRegion
argument_list|(
name|this
operator|.
name|ct
argument_list|,
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Read "
operator|+
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|MetaTask
name|writer
init|=
operator|new
name|MetaTask
argument_list|(
name|CT
argument_list|,
literal|"writer"
argument_list|)
block|{
annotation|@
name|Override
name|void
name|metaTask
parameter_list|()
throws|throws
name|Throwable
block|{
name|MetaEditor
operator|.
name|addRegionToMeta
argument_list|(
name|this
operator|.
name|ct
argument_list|,
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Wrote "
operator|+
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|reader
operator|.
name|start
argument_list|()
expr_stmt|;
name|writer
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// We're gonna check how it takes. If it takes too long, we will consider
comment|//  it as a fail. We can't put that in the @Test tag as we want to close
comment|//  the threads nicely
specifier|final
name|long
name|timeOut
init|=
literal|180000
decl_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
comment|// Make sure reader and writer are working.
name|assertTrue
argument_list|(
name|reader
operator|.
name|isProgressing
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|writer
operator|.
name|isProgressing
argument_list|()
argument_list|)
expr_stmt|;
comment|// Kill server hosting meta -- twice  . See if our reader/writer ride over the
comment|// meta moves.  They'll need to retry.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Restart="
operator|+
name|i
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|ensureSomeRegionServersAvailable
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|int
name|index
init|=
operator|-
literal|1
decl_stmt|;
do|do
block|{
name|index
operator|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getServerWithMeta
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|index
operator|==
operator|-
literal|1
operator|&&
name|startTime
operator|+
name|timeOut
operator|<
name|System
operator|.
name|currentTimeMillis
argument_list|()
condition|)
do|;
if|if
condition|(
name|index
operator|!=
operator|-
literal|1
condition|)
block|{
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|abortRegionServer
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|waitOnRegionServer
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
literal|"reader: "
operator|+
name|reader
operator|.
name|toString
argument_list|()
argument_list|,
name|reader
operator|.
name|isProgressing
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"writer: "
operator|+
name|writer
operator|.
name|toString
argument_list|()
argument_list|,
name|writer
operator|.
name|isProgressing
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
finally|finally
block|{
name|reader
operator|.
name|stop
operator|=
literal|true
expr_stmt|;
name|writer
operator|.
name|stop
operator|=
literal|true
expr_stmt|;
name|reader
operator|.
name|join
argument_list|()
expr_stmt|;
name|writer
operator|.
name|join
argument_list|()
expr_stmt|;
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|long
name|exeTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Timeout: test took "
operator|+
name|exeTime
operator|/
literal|1000
operator|+
literal|" sec"
argument_list|,
name|exeTime
operator|<
name|timeOut
argument_list|)
expr_stmt|;
block|}
comment|/**    * Thread that runs a MetaReader/MetaEditor task until asked stop.    */
specifier|abstract
specifier|static
class|class
name|MetaTask
extends|extends
name|Thread
block|{
name|boolean
name|stop
init|=
literal|false
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
name|Throwable
name|t
init|=
literal|null
decl_stmt|;
specifier|final
name|CatalogTracker
name|ct
decl_stmt|;
name|MetaTask
parameter_list|(
specifier|final
name|CatalogTracker
name|ct
parameter_list|,
specifier|final
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|ct
operator|=
name|ct
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
while|while
condition|(
operator|!
name|this
operator|.
name|stop
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Before "
operator|+
name|this
operator|.
name|getName
argument_list|()
operator|+
literal|", count="
operator|+
name|this
operator|.
name|count
argument_list|)
expr_stmt|;
name|metaTask
argument_list|()
expr_stmt|;
name|this
operator|.
name|count
operator|+=
literal|1
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"After "
operator|+
name|this
operator|.
name|getName
argument_list|()
operator|+
literal|", count="
operator|+
name|this
operator|.
name|count
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|this
operator|.
name|getName
argument_list|()
operator|+
literal|" failed"
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|this
operator|.
name|t
operator|=
name|t
expr_stmt|;
block|}
block|}
name|boolean
name|isProgressing
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|int
name|currentCount
init|=
name|this
operator|.
name|count
decl_stmt|;
while|while
condition|(
name|currentCount
operator|==
name|this
operator|.
name|count
condition|)
block|{
if|if
condition|(
operator|!
name|isAlive
argument_list|()
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|this
operator|.
name|t
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"count="
operator|+
name|this
operator|.
name|count
operator|+
literal|", t="
operator|+
operator|(
name|this
operator|.
name|t
operator|==
literal|null
condition|?
literal|"null"
else|:
name|this
operator|.
name|t
operator|.
name|toString
argument_list|()
operator|)
return|;
block|}
specifier|abstract
name|void
name|metaTask
parameter_list|()
throws|throws
name|Throwable
function_decl|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetRegionsCatalogTables
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|MetaReader
operator|.
name|getTableRegions
argument_list|(
name|CT
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|regions
operator|.
name|size
argument_list|()
operator|>=
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|MetaReader
operator|.
name|getTableRegionsAndLocations
argument_list|(
name|CT
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
argument_list|)
operator|.
name|size
argument_list|()
operator|>=
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableExists
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|String
name|name
init|=
literal|"testTableExists"
decl_stmt|;
specifier|final
name|byte
index|[]
name|nameBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|MetaReader
operator|.
name|tableExists
argument_list|(
name|CT
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|nameBytes
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|MetaReader
operator|.
name|tableExists
argument_list|(
name|CT
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|MetaReader
operator|.
name|tableExists
argument_list|(
name|CT
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|MetaReader
operator|.
name|tableExists
argument_list|(
name|CT
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetRegion
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
specifier|final
name|String
name|name
init|=
literal|"testGetRegion"
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started "
operator|+
name|name
argument_list|)
expr_stmt|;
comment|// Test get on non-existent region.
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|pair
init|=
name|MetaReader
operator|.
name|getRegion
argument_list|(
name|CT
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"nonexistent-region"
argument_list|)
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|pair
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Finished "
operator|+
name|name
argument_list|)
expr_stmt|;
block|}
comment|// Test for the optimization made in HBASE-3650
annotation|@
name|Test
specifier|public
name|void
name|testScanMetaForTable
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
specifier|final
name|String
name|name
init|=
literal|"testScanMetaForTable"
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started "
operator|+
name|name
argument_list|)
expr_stmt|;
comment|/** Create 2 tables      - testScanMetaForTable      - testScanMetaForTablf     **/
name|UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|name
argument_list|)
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
comment|// name that is +1 greater than the first one (e+1=f)
name|byte
index|[]
name|greaterName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testScanMetaForTablf"
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|greaterName
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
comment|// Now make sure we only get the regions from 1 of the tables at a time
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|MetaReader
operator|.
name|getTableRegions
argument_list|(
name|CT
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|name
argument_list|)
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|MetaReader
operator|.
name|getTableRegions
argument_list|(
name|CT
argument_list|,
name|greaterName
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|testGettingTableRegions
parameter_list|(
specifier|final
name|CatalogTracker
name|ct
parameter_list|,
specifier|final
name|byte
index|[]
name|nameBytes
parameter_list|,
specifier|final
name|int
name|regionCount
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|MetaReader
operator|.
name|getTableRegions
argument_list|(
name|ct
argument_list|,
name|nameBytes
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|regionCount
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|pair
init|=
name|MetaReader
operator|.
name|getRegion
argument_list|(
name|ct
argument_list|,
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|pair
operator|.
name|getFirst
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|regions
return|;
block|}
specifier|private
specifier|static
name|void
name|testGetRegion
parameter_list|(
specifier|final
name|CatalogTracker
name|ct
parameter_list|,
specifier|final
name|HRegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|pair
init|=
name|MetaReader
operator|.
name|getRegion
argument_list|(
name|ct
argument_list|,
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|region
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|pair
operator|.
name|getFirst
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

