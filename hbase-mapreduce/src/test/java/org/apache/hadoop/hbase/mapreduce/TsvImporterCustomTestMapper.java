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
name|mapreduce
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
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|client
operator|.
name|Durability
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
name|Put
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
name|ImmutableBytesWritable
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
name|LongWritable
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

begin_comment
comment|/**  * Dummy mapper used for unit tests to verify that the mapper can be injected.  * This approach would be used if a custom transformation needed to be done after  * reading the input data before writing it to HFiles.  */
end_comment

begin_class
specifier|public
class|class
name|TsvImporterCustomTestMapper
extends|extends
name|TsvImporterMapper
block|{
annotation|@
name|Override
specifier|protected
name|void
name|setup
parameter_list|(
name|Context
name|context
parameter_list|)
block|{
name|doSetup
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
comment|/**    * Convert a line of TSV text into an HBase table row after transforming the    * values by multiplying them by 3.    */
annotation|@
name|Override
specifier|public
name|void
name|map
parameter_list|(
name|LongWritable
name|offset
parameter_list|,
name|Text
name|value
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"FAM"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
index|[]
name|qualifiers
init|=
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"B"
argument_list|)
block|}
decl_stmt|;
comment|// do some basic line parsing
name|byte
index|[]
name|lineBytes
init|=
name|value
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|String
index|[]
name|valueTokens
init|=
operator|new
name|String
argument_list|(
name|lineBytes
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
operator|.
name|split
argument_list|(
literal|"\u001b"
argument_list|)
decl_stmt|;
comment|// create the rowKey and Put
name|ImmutableBytesWritable
name|rowKey
init|=
operator|new
name|ImmutableBytesWritable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|valueTokens
index|[
literal|0
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|rowKey
operator|.
name|copyBytes
argument_list|()
argument_list|)
decl_stmt|;
name|put
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
comment|//The value should look like this: VALUE1 or VALUE2. Let's multiply
comment|//the integer by 3
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|valueTokens
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|prefix
init|=
name|valueTokens
index|[
name|i
index|]
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|"VALUE"
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|suffix
init|=
name|valueTokens
index|[
name|i
index|]
operator|.
name|substring
argument_list|(
literal|"VALUE"
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|newValue
init|=
name|prefix
operator|+
name|Integer
operator|.
name|parseInt
argument_list|(
name|suffix
argument_list|)
operator|*
literal|3
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|rowKey
operator|.
name|copyBytes
argument_list|()
argument_list|,
name|family
argument_list|,
name|qualifiers
index|[
name|i
operator|-
literal|1
index|]
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|newValue
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|context
operator|.
name|write
argument_list|(
name|rowKey
argument_list|,
name|put
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

