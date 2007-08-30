begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|log4j
operator|.
name|Level
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
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
name|HTable
name|table
decl_stmt|;
comment|/** constructor */
specifier|public
name|TestCleanRegionServerExit
parameter_list|()
block|{
name|super
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"ipc.client.timeout"
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
comment|// reduce ipc client timeout
name|conf
operator|.
name|setInt
argument_list|(
literal|"ipc.client.connect.max.retries"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
comment|// and number of retries
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.client.retries.number"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
comment|// reduce HBase retries
name|Logger
operator|.
name|getRootLogger
argument_list|()
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|WARN
argument_list|)
expr_stmt|;
name|Logger
operator|.
name|getLogger
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getPackage
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|DEBUG
argument_list|)
expr_stmt|;
block|}
comment|/**    * {@inheritDoc}    */
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
block|}
comment|/**    * The test    * @throws IOException    */
specifier|public
name|void
name|testCleanRegionServerExit
parameter_list|()
throws|throws
name|IOException
block|{
comment|// When the META table can be opened, the region servers are running
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|HTable
name|meta
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
comment|// Put something into the meta table.
name|String
name|tableName
init|=
name|getName
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY
operator|.
name|toString
argument_list|()
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
comment|// put some values in the table
name|this
operator|.
name|table
operator|=
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
expr_stmt|;
name|Text
name|row
init|=
operator|new
name|Text
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
name|long
name|lockid
init|=
name|table
operator|.
name|startUpdate
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|table
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|HConstants
operator|.
name|COLUMN_FAMILY
argument_list|,
name|tableName
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|commit
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
comment|// Start up a new region server to take over serving of root and meta
comment|// after we shut down the current meta/root host.
name|this
operator|.
name|cluster
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
comment|// Now shutdown the region server and wait for it to go down.
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
name|waitOnRegionServer
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// Verify that the client can find the data after the region has been moved
comment|// to a different server
name|HScannerInterface
name|scanner
init|=
name|table
operator|.
name|obtainScanner
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY_ARRAY
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|results
argument_list|)
condition|)
block|{
name|assertTrue
argument_list|(
name|key
operator|.
name|getRow
argument_list|()
operator|.
name|equals
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|bytes
init|=
name|results
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tableName
operator|.
name|equals
argument_list|(
operator|new
name|String
argument_list|(
name|bytes
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Success!"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

