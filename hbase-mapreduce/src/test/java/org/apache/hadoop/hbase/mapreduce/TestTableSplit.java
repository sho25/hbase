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
name|util
operator|.
name|HashSet
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
name|HBaseClassTestRule
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
name|TableName
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
name|testclassification
operator|.
name|MapReduceTests
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
name|testclassification
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestName
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MapReduceTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestTableSplit
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestTableSplit
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testHashCode
parameter_list|()
block|{
name|TableSplit
name|split1
init|=
operator|new
name|TableSplit
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-start"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-end"
argument_list|)
argument_list|,
literal|"location"
argument_list|)
decl_stmt|;
name|TableSplit
name|split2
init|=
operator|new
name|TableSplit
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-start"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-end"
argument_list|)
argument_list|,
literal|"location"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|split1
argument_list|,
name|split2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|split1
operator|.
name|hashCode
argument_list|()
operator|==
name|split2
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|HashSet
argument_list|<
name|TableSplit
argument_list|>
name|set
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|set
operator|.
name|add
argument_list|(
name|split1
argument_list|)
expr_stmt|;
name|set
operator|.
name|add
argument_list|(
name|split2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|set
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * length of region should not influence hashcode    * */
annotation|@
name|Test
specifier|public
name|void
name|testHashCode_length
parameter_list|()
block|{
name|TableSplit
name|split1
init|=
operator|new
name|TableSplit
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-start"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-end"
argument_list|)
argument_list|,
literal|"location"
argument_list|,
literal|1984
argument_list|)
decl_stmt|;
name|TableSplit
name|split2
init|=
operator|new
name|TableSplit
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-start"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-end"
argument_list|)
argument_list|,
literal|"location"
argument_list|,
literal|1982
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|split1
argument_list|,
name|split2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|split1
operator|.
name|hashCode
argument_list|()
operator|==
name|split2
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|HashSet
argument_list|<
name|TableSplit
argument_list|>
name|set
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|set
operator|.
name|add
argument_list|(
name|split1
argument_list|)
expr_stmt|;
name|set
operator|.
name|add
argument_list|(
name|split2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|set
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Length of region need to be properly serialized.    * */
annotation|@
name|Test
specifier|public
name|void
name|testLengthIsSerialized
parameter_list|()
throws|throws
name|Exception
block|{
name|TableSplit
name|split1
init|=
operator|new
name|TableSplit
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-start"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-end"
argument_list|)
argument_list|,
literal|"location"
argument_list|,
literal|666
argument_list|)
decl_stmt|;
name|TableSplit
name|deserialized
init|=
operator|new
name|TableSplit
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-start2"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-end2"
argument_list|)
argument_list|,
literal|"location1"
argument_list|)
decl_stmt|;
name|ReflectionUtils
operator|.
name|copy
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
name|split1
argument_list|,
name|deserialized
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|666
argument_list|,
name|deserialized
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testToString
parameter_list|()
block|{
name|TableSplit
name|split
init|=
operator|new
name|TableSplit
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-start"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-end"
argument_list|)
argument_list|,
literal|"location"
argument_list|)
decl_stmt|;
name|String
name|str
init|=
literal|"HBase table split(table name: "
operator|+
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|", scan: , start row: row-start, "
operator|+
literal|"end row: row-end, region location: location, "
operator|+
literal|"encoded region name: )"
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|str
argument_list|,
name|split
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|split
operator|=
operator|new
name|TableSplit
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-start"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-end"
argument_list|)
argument_list|,
literal|"location"
argument_list|,
literal|"encoded-region-name"
argument_list|,
literal|1000L
argument_list|)
expr_stmt|;
name|str
operator|=
literal|"HBase table split(table name: "
operator|+
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|", scan: , start row: row-start, "
operator|+
literal|"end row: row-end, region location: location, "
operator|+
literal|"encoded region name: encoded-region-name)"
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|str
argument_list|,
name|split
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|split
operator|=
operator|new
name|TableSplit
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|str
operator|=
literal|"HBase table split(table name: null, scan: , start row: null, "
operator|+
literal|"end row: null, region location: null, "
operator|+
literal|"encoded region name: )"
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|str
argument_list|,
name|split
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|split
operator|=
operator|new
name|TableSplit
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|1000L
argument_list|)
expr_stmt|;
name|str
operator|=
literal|"HBase table split(table name: null, scan: , start row: null, "
operator|+
literal|"end row: null, region location: null, "
operator|+
literal|"encoded region name: null)"
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|str
argument_list|,
name|split
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

