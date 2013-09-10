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
name|hbase
operator|.
name|CellScanner
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
name|ipc
operator|.
name|RpcClient
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
comment|/**  * Do some ops and prove that client and server can work w/o codecs; that we can pb all the time.  * Good for third-party clients or simple scripts that want to talk direct to hbase.  */
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
name|TestFromClientSideNoCodec
block|{
specifier|protected
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Turn off codec use
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
literal|"hbase.client.default.rpc.codec"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBasics
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|byte
index|[]
name|t
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testBasics"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
index|[]
name|fs
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf1"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf2"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf3"
argument_list|)
block|}
decl_stmt|;
name|HTable
name|ht
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|t
argument_list|,
name|fs
argument_list|)
decl_stmt|;
comment|// Check put and get.
specifier|final
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|f
range|:
name|fs
control|)
name|p
operator|.
name|add
argument_list|(
name|f
argument_list|,
name|f
argument_list|,
name|f
argument_list|)
expr_stmt|;
name|ht
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|ht
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|row
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|CellScanner
name|cellScanner
init|=
name|r
operator|.
name|cellScanner
argument_list|()
init|;
name|cellScanner
operator|.
name|advance
argument_list|()
condition|;
control|)
block|{
name|Cell
name|cell
init|=
name|cellScanner
operator|.
name|current
argument_list|()
decl_stmt|;
name|byte
index|[]
name|f
init|=
name|fs
index|[
name|i
operator|++
index|]
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|f
argument_list|)
argument_list|,
name|Bytes
operator|.
name|equals
argument_list|(
name|cell
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|f
argument_list|,
literal|0
argument_list|,
name|f
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Check getRowOrBefore
name|byte
index|[]
name|f
init|=
name|fs
index|[
literal|0
index|]
decl_stmt|;
name|r
operator|=
name|ht
operator|.
name|getRowOrBefore
argument_list|(
name|row
argument_list|,
name|f
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|r
operator|.
name|toString
argument_list|()
argument_list|,
name|r
operator|.
name|containsColumn
argument_list|(
name|f
argument_list|,
name|f
argument_list|)
argument_list|)
expr_stmt|;
comment|// Check scan.
name|ResultScanner
name|scanner
init|=
name|ht
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|(
name|r
operator|=
name|scanner
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|assertTrue
argument_list|(
name|r
operator|.
name|listCells
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|3
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|count
operator|==
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoCodec
parameter_list|()
block|{
name|Configuration
name|c
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|c
operator|.
name|set
argument_list|(
literal|"hbase.client.default.rpc.codec"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|String
name|codec
init|=
name|RpcClient
operator|.
name|getDefaultCodec
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|codec
operator|==
literal|null
operator|||
name|codec
operator|.
name|length
argument_list|()
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

