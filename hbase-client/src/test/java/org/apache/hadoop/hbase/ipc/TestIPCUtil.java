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
name|ipc
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
name|nio
operator|.
name|ByteBuffer
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
name|CellUtil
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
name|KeyValue
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
name|SmallTests
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
name|codec
operator|.
name|Codec
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
name|codec
operator|.
name|KeyValueCodec
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
name|io
operator|.
name|compress
operator|.
name|CompressionCodec
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
name|compress
operator|.
name|DefaultCodec
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
name|compress
operator|.
name|GzipCodec
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestIPCUtil
block|{
name|IPCUtil
name|util
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
block|{
name|this
operator|.
name|util
operator|=
operator|new
name|IPCUtil
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBuildCellBlock
parameter_list|()
throws|throws
name|IOException
block|{
name|doBuildCellBlockUndoCellBlock
argument_list|(
operator|new
name|KeyValueCodec
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|doBuildCellBlockUndoCellBlock
argument_list|(
operator|new
name|KeyValueCodec
argument_list|()
argument_list|,
operator|new
name|DefaultCodec
argument_list|()
argument_list|)
expr_stmt|;
name|doBuildCellBlockUndoCellBlock
argument_list|(
operator|new
name|KeyValueCodec
argument_list|()
argument_list|,
operator|new
name|GzipCodec
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|void
name|doBuildCellBlockUndoCellBlock
parameter_list|(
specifier|final
name|Codec
name|codec
parameter_list|,
specifier|final
name|CompressionCodec
name|compressor
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|int
name|count
init|=
literal|10
decl_stmt|;
name|Cell
index|[]
name|cells
init|=
name|getCells
argument_list|(
name|count
argument_list|)
decl_stmt|;
name|ByteBuffer
name|bb
init|=
name|this
operator|.
name|util
operator|.
name|buildCellBlock
argument_list|(
name|codec
argument_list|,
name|compressor
argument_list|,
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|cells
argument_list|)
operator|.
name|iterator
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|CellScanner
name|scanner
init|=
name|this
operator|.
name|util
operator|.
name|createCellScanner
argument_list|(
name|codec
argument_list|,
name|compressor
argument_list|,
name|bb
operator|.
name|array
argument_list|()
argument_list|,
literal|0
argument_list|,
name|bb
operator|.
name|limit
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|advance
argument_list|()
condition|)
block|{
name|i
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|count
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
specifier|static
name|Cell
index|[]
name|getCells
parameter_list|(
specifier|final
name|int
name|howMany
parameter_list|)
block|{
name|Cell
index|[]
name|cells
init|=
operator|new
name|Cell
index|[
name|howMany
index|]
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
name|howMany
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|index
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|index
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
argument_list|,
name|index
argument_list|,
name|index
argument_list|)
decl_stmt|;
name|cells
index|[
name|i
index|]
operator|=
name|kv
expr_stmt|;
block|}
return|return
name|cells
return|;
block|}
block|}
end_class

end_unit

