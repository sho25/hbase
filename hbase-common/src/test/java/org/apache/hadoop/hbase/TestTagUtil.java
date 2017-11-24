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
name|util
operator|.
name|List
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
name|MiscTests
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
block|{
name|MiscTests
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
name|TestTagUtil
block|{
annotation|@
name|Test
specifier|public
name|void
name|testCarryForwardTTLTag
parameter_list|()
throws|throws
name|Exception
block|{
comment|// No tags so far and the TTL tag must get added to the Tags list
name|long
name|ttl
init|=
literal|10
operator|*
literal|1000
decl_stmt|;
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
init|=
name|TagUtil
operator|.
name|carryForwardTTLTag
argument_list|(
literal|null
argument_list|,
name|ttl
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tags
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Tag
name|ttlTag
init|=
name|tags
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|TagType
operator|.
name|TTL_TAG_TYPE
argument_list|,
name|ttlTag
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ttl
argument_list|,
name|Tag
operator|.
name|getValueAsLong
argument_list|(
name|ttlTag
argument_list|)
argument_list|)
expr_stmt|;
comment|// Already having a TTL tag in the list. So the call must remove the old tag
name|long
name|ttl2
init|=
literal|30
operator|*
literal|1000
decl_stmt|;
name|tags
operator|=
name|TagUtil
operator|.
name|carryForwardTTLTag
argument_list|(
name|tags
argument_list|,
name|ttl2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tags
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|ttlTag
operator|=
name|tags
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TagType
operator|.
name|TTL_TAG_TYPE
argument_list|,
name|ttlTag
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ttl2
argument_list|,
name|Tag
operator|.
name|getValueAsLong
argument_list|(
name|ttlTag
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

