begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2006 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/**  * Tests region server failover when a region server exits.  */
end_comment

begin_class
specifier|public
class|class
name|TestCleanRegionServerExit
extends|extends
name|HBaseClusterTestCase
block|{
specifier|private
name|HClient
name|client
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|this
operator|.
name|client
operator|=
operator|new
name|HClient
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testCleanRegionServerExit
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
try|try
block|{
comment|// When the META table can be opened, the region servers are running
name|this
operator|.
name|client
operator|.
name|openTable
argument_list|(
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
comment|// Put something into the meta table.
name|this
operator|.
name|client
operator|.
name|createTable
argument_list|(
operator|new
name|HTableDescriptor
argument_list|(
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Get current region server instance.
name|HRegionServer
name|hsr
init|=
name|this
operator|.
name|cluster
operator|.
name|regionServers
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Thread
name|hrst
init|=
name|this
operator|.
name|cluster
operator|.
name|regionThreads
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// Start up a new one to take over serving of root and meta after we shut
comment|// down the current meta/root host.
name|this
operator|.
name|cluster
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
comment|// Now shutdown the region server and wait for it to go down.
name|hsr
operator|.
name|stop
argument_list|()
expr_stmt|;
name|hrst
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// The recalibration of the client is not working properly.  FIX.
comment|// After above is fixed, add in assertions that we can get data from
comment|// newly located meta table.
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
comment|/* Comment out till recalibration of client is working properly.    public void testRegionServerAbort()   throws IOException, InterruptedException {     // When the META table can be opened, the region servers are running     this.client.openTable(HConstants.META_TABLE_NAME);     // Put something into the meta table.     this.client.createTable(new HTableDescriptor(getName()));     // Get current region server instance.     HRegionServer hsr = this.cluster.regionServers.get(0);     Thread hrst = this.cluster.regionThreads.get(0);     // Start up a new one to take over serving of root and meta after we shut     // down the current meta/root host.     this.cluster.startRegionServer();     // Force a region server to exit "ungracefully"     hsr.abort();     hrst.join();     // The recalibration of the client is not working properly.  FIX.     // After above is fixed, add in assertions that we can get data from     // newly located meta table.   } */
block|}
end_class

end_unit

