begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|*
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
comment|/**  * Test of simple partitioner.  */
end_comment

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
name|TestSimpleTotalOrderPartitioner
extends|extends
name|HBaseTestCase
block|{
specifier|public
name|void
name|testSplit
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|start
init|=
literal|"a"
decl_stmt|;
name|String
name|end
init|=
literal|"{"
decl_stmt|;
name|SimpleTotalOrderPartitioner
argument_list|<
name|byte
index|[]
argument_list|>
name|p
init|=
operator|new
name|SimpleTotalOrderPartitioner
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
name|SimpleTotalOrderPartitioner
operator|.
name|START
argument_list|,
name|start
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
name|SimpleTotalOrderPartitioner
operator|.
name|END
argument_list|,
name|end
argument_list|)
expr_stmt|;
name|p
operator|.
name|setConf
argument_list|(
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
name|ImmutableBytesWritable
name|c
init|=
operator|new
name|ImmutableBytesWritable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
argument_list|)
argument_list|)
decl_stmt|;
comment|// If one reduce, partition should be 0.
name|int
name|partition
init|=
name|p
operator|.
name|getPartition
argument_list|(
name|c
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|partition
argument_list|)
expr_stmt|;
comment|// If two reduces, partition should be 0.
name|partition
operator|=
name|p
operator|.
name|getPartition
argument_list|(
name|c
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|partition
argument_list|)
expr_stmt|;
comment|// Divide in 3.
name|partition
operator|=
name|p
operator|.
name|getPartition
argument_list|(
name|c
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|partition
argument_list|)
expr_stmt|;
name|ImmutableBytesWritable
name|q
init|=
operator|new
name|ImmutableBytesWritable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
argument_list|)
decl_stmt|;
name|partition
operator|=
name|p
operator|.
name|getPartition
argument_list|(
name|q
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|partition
argument_list|)
expr_stmt|;
name|partition
operator|=
name|p
operator|.
name|getPartition
argument_list|(
name|q
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|partition
argument_list|)
expr_stmt|;
comment|// What about end and start keys.
name|ImmutableBytesWritable
name|startBytes
init|=
operator|new
name|ImmutableBytesWritable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|start
argument_list|)
argument_list|)
decl_stmt|;
name|partition
operator|=
name|p
operator|.
name|getPartition
argument_list|(
name|startBytes
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|partition
argument_list|)
expr_stmt|;
name|partition
operator|=
name|p
operator|.
name|getPartition
argument_list|(
name|startBytes
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|partition
argument_list|)
expr_stmt|;
name|ImmutableBytesWritable
name|endBytes
init|=
operator|new
name|ImmutableBytesWritable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"z"
argument_list|)
argument_list|)
decl_stmt|;
name|partition
operator|=
name|p
operator|.
name|getPartition
argument_list|(
name|endBytes
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|partition
argument_list|)
expr_stmt|;
name|partition
operator|=
name|p
operator|.
name|getPartition
argument_list|(
name|endBytes
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|partition
argument_list|)
expr_stmt|;
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

