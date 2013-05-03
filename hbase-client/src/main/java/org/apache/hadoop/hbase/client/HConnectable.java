begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
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

begin_comment
comment|/**  * This class makes it convenient for one to execute a command in the context  * of a {@link HConnection} instance based on the given {@link Configuration}.  *  *<p>  * If you find yourself wanting to use a {@link HConnection} for a relatively  * short duration of time, and do not want to deal with the hassle of creating  * and cleaning up that resource, then you should consider using this  * convenience class.  *  * @param<T>  *          the return type of the {@link HConnectable#connect(HConnection)}  *          method.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|HConnectable
parameter_list|<
name|T
parameter_list|>
block|{
specifier|public
name|Configuration
name|conf
decl_stmt|;
specifier|protected
name|HConnectable
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
specifier|public
specifier|abstract
name|T
name|connect
parameter_list|(
name|HConnection
name|connection
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

