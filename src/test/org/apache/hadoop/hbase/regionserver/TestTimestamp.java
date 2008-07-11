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
operator|.
name|CompressionType
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
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestTimestamp
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COLUMN_NAME
init|=
literal|"contents:"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_NAME
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|VERSIONS
init|=
literal|3
decl_stmt|;
comment|/**    * Test that delete works according to description in<a    * href="https://issues.apache.org/jira/browse/HADOOP-1784">hadoop-1784</a>.    * @throws IOException    */
specifier|public
name|void
name|testDelete
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|HRegion
name|r
init|=
name|createRegion
argument_list|()
decl_stmt|;
try|try
block|{
specifier|final
name|HRegionIncommon
name|region
init|=
operator|new
name|HRegionIncommon
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|TimestampTestBase
operator|.
name|doTestDelete
argument_list|(
name|region
argument_list|,
name|region
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
name|r
operator|.
name|getLog
argument_list|()
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"testDelete() finished"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test scanning against different timestamps.    * @throws IOException    */
specifier|public
name|void
name|testTimestampScanning
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|HRegion
name|r
init|=
name|createRegion
argument_list|()
decl_stmt|;
try|try
block|{
specifier|final
name|HRegionIncommon
name|region
init|=
operator|new
name|HRegionIncommon
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|TimestampTestBase
operator|.
name|doTestTimestampScanning
argument_list|(
name|region
argument_list|,
name|region
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
name|r
operator|.
name|getLog
argument_list|()
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"testTimestampScanning() finished"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HRegion
name|createRegion
parameter_list|()
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|htd
init|=
name|createTableDescriptor
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|COLUMN
argument_list|,
name|VERSIONS
argument_list|,
name|CompressionType
operator|.
name|NONE
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|HConstants
operator|.
name|FOREVER
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|createNewHRegion
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
block|}
end_class

end_unit

