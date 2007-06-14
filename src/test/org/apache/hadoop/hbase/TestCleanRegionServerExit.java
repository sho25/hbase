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
comment|/** Tests region server failover when a region server exits cleanly */
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
comment|/** Constructor */
specifier|public
name|TestCleanRegionServerExit
parameter_list|()
block|{
name|super
argument_list|(
literal|2
argument_list|)
expr_stmt|;
comment|// Start two region servers
name|client
operator|=
operator|new
name|HClient
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/** The test     * @throws IOException     * @throws InterruptedException */
specifier|public
name|void
name|testCleanRegionServerExit
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
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
name|this
operator|.
name|cluster
operator|.
name|stopRegionServer
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|cluster
operator|.
name|regionThreads
index|[
literal|0
index|]
operator|.
name|join
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|60000
argument_list|)
expr_stmt|;
comment|// Wait for cluster to adjust
block|}
block|}
end_class

end_unit

