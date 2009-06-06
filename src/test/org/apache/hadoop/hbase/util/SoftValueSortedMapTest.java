begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedMap
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

begin_class
specifier|public
class|class
name|SoftValueSortedMapTest
block|{
specifier|private
specifier|static
name|void
name|testMap
parameter_list|(
name|SortedMap
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|map
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Testing "
operator|+
name|map
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|1000000
condition|;
name|i
operator|++
control|)
block|{
name|map
operator|.
name|put
argument_list|(
operator|new
name|Integer
argument_list|(
name|i
argument_list|)
argument_list|,
operator|new
name|Integer
argument_list|(
name|i
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
name|map
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|byte
index|[]
name|block
init|=
operator|new
name|byte
index|[
literal|849
operator|*
literal|1024
operator|*
literal|1024
index|]
decl_stmt|;
comment|// 10 MB
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|map
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|testMap
argument_list|(
operator|new
name|SoftValueSortedMap
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|testMap
argument_list|(
operator|new
name|TreeMap
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

