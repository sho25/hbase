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
name|tool
operator|.
name|coprocessor
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CoprocessorMethods
block|{
specifier|private
specifier|final
name|Set
argument_list|<
name|CoprocessorMethod
argument_list|>
name|methods
decl_stmt|;
specifier|public
name|CoprocessorMethods
parameter_list|()
block|{
name|methods
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|addMethod
parameter_list|(
name|String
name|name
parameter_list|,
name|String
modifier|...
name|parameters
parameter_list|)
block|{
name|CoprocessorMethod
name|cpMethod
init|=
operator|new
name|CoprocessorMethod
argument_list|(
name|name
argument_list|)
operator|.
name|withParameters
argument_list|(
name|parameters
argument_list|)
decl_stmt|;
name|methods
operator|.
name|add
argument_list|(
name|cpMethod
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addMethod
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
modifier|...
name|parameters
parameter_list|)
block|{
name|CoprocessorMethod
name|cpMethod
init|=
operator|new
name|CoprocessorMethod
argument_list|(
name|name
argument_list|)
operator|.
name|withParameters
argument_list|(
name|parameters
argument_list|)
decl_stmt|;
name|methods
operator|.
name|add
argument_list|(
name|cpMethod
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addMethod
parameter_list|(
name|Method
name|method
parameter_list|)
block|{
name|CoprocessorMethod
name|cpMethod
init|=
operator|new
name|CoprocessorMethod
argument_list|(
name|method
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|withParameters
argument_list|(
name|method
operator|.
name|getParameterTypes
argument_list|()
argument_list|)
decl_stmt|;
name|methods
operator|.
name|add
argument_list|(
name|cpMethod
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasMethod
parameter_list|(
name|String
name|name
parameter_list|,
name|String
modifier|...
name|parameters
parameter_list|)
block|{
name|CoprocessorMethod
name|method
init|=
operator|new
name|CoprocessorMethod
argument_list|(
name|name
argument_list|)
operator|.
name|withParameters
argument_list|(
name|parameters
argument_list|)
decl_stmt|;
return|return
name|methods
operator|.
name|contains
argument_list|(
name|method
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|hasMethod
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
modifier|...
name|parameters
parameter_list|)
block|{
name|CoprocessorMethod
name|method
init|=
operator|new
name|CoprocessorMethod
argument_list|(
name|name
argument_list|)
operator|.
name|withParameters
argument_list|(
name|parameters
argument_list|)
decl_stmt|;
return|return
name|methods
operator|.
name|contains
argument_list|(
name|method
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|hasMethod
parameter_list|(
name|Method
name|method
parameter_list|)
block|{
name|CoprocessorMethod
name|cpMethod
init|=
operator|new
name|CoprocessorMethod
argument_list|(
name|method
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|withParameters
argument_list|(
name|method
operator|.
name|getParameterTypes
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|methods
operator|.
name|contains
argument_list|(
name|cpMethod
argument_list|)
return|;
block|}
block|}
end_class

end_unit
