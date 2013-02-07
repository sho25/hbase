begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|timestamp
operator|.
name|data
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|hbase
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|timestamp
operator|.
name|TestTimestampData
import|;
end_import

begin_class
specifier|public
class|class
name|TestTimestampDataRepeats
implements|implements
name|TestTimestampData
block|{
specifier|private
specifier|static
name|long
name|t
init|=
literal|1234567890L
decl_stmt|;
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Long
argument_list|>
name|getInputs
parameter_list|()
block|{
name|List
argument_list|<
name|Long
argument_list|>
name|d
init|=
operator|new
name|ArrayList
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|d
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
return|return
name|d
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMinimum
parameter_list|()
block|{
return|return
name|t
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Long
argument_list|>
name|getOutputs
parameter_list|()
block|{
name|List
argument_list|<
name|Long
argument_list|>
name|d
init|=
operator|new
name|ArrayList
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
return|return
name|d
return|;
block|}
block|}
end_class

end_unit

