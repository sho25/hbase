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
name|rest
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|HBaseConfiguration
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
name|HBaseAdmin
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
name|rest
operator|.
name|exception
operator|.
name|HBaseRestException
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
name|rest
operator|.
name|parser
operator|.
name|IHBaseRestParser
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

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractController
implements|implements
name|RESTConstants
block|{
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|protected
name|AbstractModel
name|model
decl_stmt|;
specifier|public
name|void
name|initialize
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|,
name|HBaseAdmin
name|admin
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|model
operator|=
name|generateModel
argument_list|(
name|conf
argument_list|,
name|admin
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|abstract
name|void
name|get
parameter_list|(
name|Status
name|s
parameter_list|,
name|byte
index|[]
index|[]
name|pathSegments
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|queryMap
parameter_list|)
throws|throws
name|HBaseRestException
function_decl|;
specifier|public
specifier|abstract
name|void
name|post
parameter_list|(
name|Status
name|s
parameter_list|,
name|byte
index|[]
index|[]
name|pathSegments
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|queryMap
parameter_list|,
name|byte
index|[]
name|input
parameter_list|,
name|IHBaseRestParser
name|parser
parameter_list|)
throws|throws
name|HBaseRestException
function_decl|;
specifier|public
specifier|abstract
name|void
name|put
parameter_list|(
name|Status
name|s
parameter_list|,
name|byte
index|[]
index|[]
name|pathSegments
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|queryMap
parameter_list|,
name|byte
index|[]
name|input
parameter_list|,
name|IHBaseRestParser
name|parser
parameter_list|)
throws|throws
name|HBaseRestException
function_decl|;
specifier|public
specifier|abstract
name|void
name|delete
parameter_list|(
name|Status
name|s
parameter_list|,
name|byte
index|[]
index|[]
name|pathSegments
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|queryMap
parameter_list|)
throws|throws
name|HBaseRestException
function_decl|;
specifier|protected
specifier|abstract
name|AbstractModel
name|generateModel
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|,
name|HBaseAdmin
name|a
parameter_list|)
function_decl|;
specifier|protected
name|byte
index|[]
index|[]
name|getColumnsFromQueryMap
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|queryMap
parameter_list|)
block|{
name|byte
index|[]
index|[]
name|columns
init|=
literal|null
decl_stmt|;
name|String
index|[]
name|columnArray
init|=
name|queryMap
operator|.
name|get
argument_list|(
name|RESTConstants
operator|.
name|COLUMN
argument_list|)
decl_stmt|;
if|if
condition|(
name|columnArray
operator|!=
literal|null
condition|)
block|{
name|columns
operator|=
operator|new
name|byte
index|[
name|columnArray
operator|.
name|length
index|]
index|[]
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
name|columnArray
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|columns
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|columnArray
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|columns
return|;
block|}
block|}
end_class

end_unit

