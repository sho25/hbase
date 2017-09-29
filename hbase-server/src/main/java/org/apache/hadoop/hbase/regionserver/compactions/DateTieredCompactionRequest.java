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
operator|.
name|compactions
package|;
end_package

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
name|java
operator|.
name|util
operator|.
name|Collection
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
name|regionserver
operator|.
name|HStoreFile
import|;
end_import

begin_class
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"EQ_DOESNT_OVERRIDE_EQUALS"
argument_list|,
name|justification
operator|=
literal|"It is intended to use the same equal method as superclass"
argument_list|)
specifier|public
class|class
name|DateTieredCompactionRequest
extends|extends
name|CompactionRequestImpl
block|{
specifier|private
name|List
argument_list|<
name|Long
argument_list|>
name|boundaries
decl_stmt|;
specifier|public
name|DateTieredCompactionRequest
parameter_list|(
name|Collection
argument_list|<
name|HStoreFile
argument_list|>
name|files
parameter_list|,
name|List
argument_list|<
name|Long
argument_list|>
name|boundaryList
parameter_list|)
block|{
name|super
argument_list|(
name|files
argument_list|)
expr_stmt|;
name|boundaries
operator|=
name|boundaryList
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Long
argument_list|>
name|getBoundaries
parameter_list|()
block|{
return|return
name|boundaries
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|super
operator|.
name|toString
argument_list|()
operator|+
literal|" boundaries="
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|boundaries
operator|.
name|toArray
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

