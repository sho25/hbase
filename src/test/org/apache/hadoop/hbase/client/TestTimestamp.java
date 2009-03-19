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
name|TimestampTestBase
import|;
end_import

begin_comment
comment|/**  * Tests user specifiable time stamps putting, getting and scanning.  Also  * tests same in presence of deletes.  Test cores are written so can be  * run against an HRegion and against an HTable: i.e. both local and remote.  */
end_comment

begin_class
specifier|public
class|class
name|TestTimestamp
extends|extends
name|HBaseClusterTestCase
block|{
specifier|private
specifier|static
specifier|final
name|String
name|COLUMN_NAME
init|=
literal|"contents:"
decl_stmt|;
comment|/** constructor */
specifier|public
name|TestTimestamp
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Basic test of timestamps.    * Do the above tests from client side.    * @throws IOException    */
specifier|public
name|void
name|testTimestamps
parameter_list|()
throws|throws
name|IOException
block|{
name|HTable
name|t
init|=
name|createTable
argument_list|()
decl_stmt|;
name|Incommon
name|incommon
init|=
operator|new
name|HTableIncommon
argument_list|(
name|t
argument_list|)
decl_stmt|;
name|TimestampTestBase
operator|.
name|doTestDelete
argument_list|(
name|incommon
argument_list|,
operator|new
name|FlushCache
argument_list|()
block|{
specifier|public
name|void
name|flushcache
parameter_list|()
throws|throws
name|IOException
block|{
name|cluster
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// Perhaps drop and readd the table between tests so the former does
comment|// not pollute this latter?  Or put into separate tests.
name|TimestampTestBase
operator|.
name|doTestTimestampScanning
argument_list|(
name|incommon
argument_list|,
operator|new
name|FlushCache
argument_list|()
block|{
specifier|public
name|void
name|flushcache
parameter_list|()
throws|throws
name|IOException
block|{
name|cluster
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/*     * Create a table named TABLE_NAME.    * @return An instance of an HTable connected to the created table.    * @throws IOException    */
specifier|private
name|HTable
name|createTable
parameter_list|()
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|COLUMN_NAME
argument_list|)
argument_list|)
expr_stmt|;
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
name|desc
argument_list|)
expr_stmt|;
return|return
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|getName
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

