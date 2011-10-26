begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
operator|.
name|handler
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
name|assertNotNull
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
name|Server
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
name|regionserver
operator|.
name|RegionServerServices
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
name|MockRegionServerServices
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
name|MockServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
operator|.
name|NodeExistsException
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
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_comment
comment|/**  * Test of the {@link CloseRegionHandler}.  */
end_comment

begin_class
specifier|public
class|class
name|TestCloseRegionHandler
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
name|TestCloseRegionHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|HTU
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
comment|/**    * Test that if we fail a flush, abort gets set on close.    * @see<a href="https://issues.apache.org/jira/browse/HBASE-4270">HBASE-4270</a>    * @throws IOException    * @throws NodeExistsException    * @throws KeeperException    */
annotation|@
name|Test
specifier|public
name|void
name|testFailedFlushAborts
parameter_list|()
throws|throws
name|IOException
throws|,
name|NodeExistsException
throws|,
name|KeeperException
block|{
specifier|final
name|Server
name|server
init|=
operator|new
name|MockServer
argument_list|(
name|HTU
argument_list|,
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|RegionServerServices
name|rss
init|=
operator|new
name|MockRegionServerServices
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"testFailedFlushAborts"
argument_list|)
decl_stmt|;
specifier|final
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|hri
argument_list|,
name|HTU
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|htd
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|region
argument_list|)
expr_stmt|;
comment|// Spy on the region so can throw exception when close is called.
name|HRegion
name|spy
init|=
name|Mockito
operator|.
name|spy
argument_list|(
name|region
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|abort
init|=
literal|false
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|spy
operator|.
name|close
argument_list|(
name|abort
argument_list|)
argument_list|)
operator|.
name|thenThrow
argument_list|(
operator|new
name|RuntimeException
argument_list|(
literal|"Mocked failed close!"
argument_list|)
argument_list|)
expr_stmt|;
comment|// The CloseRegionHandler will try to get an HRegion that corresponds
comment|// to the passed hri -- so insert the region into the online region Set.
name|rss
operator|.
name|addToOnlineRegions
argument_list|(
name|spy
argument_list|)
expr_stmt|;
comment|// Assert the Server is NOT stopped before we call close region.
name|assertFalse
argument_list|(
name|server
operator|.
name|isStopped
argument_list|()
argument_list|)
expr_stmt|;
name|CloseRegionHandler
name|handler
init|=
operator|new
name|CloseRegionHandler
argument_list|(
name|server
argument_list|,
name|rss
argument_list|,
name|hri
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|boolean
name|throwable
init|=
literal|false
decl_stmt|;
try|try
block|{
name|handler
operator|.
name|process
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|throwable
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
name|assertTrue
argument_list|(
name|throwable
argument_list|)
expr_stmt|;
comment|// Abort calls stop so stopped flag should be set.
name|assertTrue
argument_list|(
name|server
operator|.
name|isStopped
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

