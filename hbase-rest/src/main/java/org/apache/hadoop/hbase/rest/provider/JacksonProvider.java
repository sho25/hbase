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
name|rest
operator|.
name|provider
package|;
end_package

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|jaxrs
operator|.
name|JacksonJaxbJsonProvider
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|ext
operator|.
name|Provider
import|;
end_import

begin_comment
comment|//create a class in the defined resource package name
end_comment

begin_comment
comment|//so it gets activated
end_comment

begin_comment
comment|//Use jackson to take care of json
end_comment

begin_comment
comment|//since it has better support for object
end_comment

begin_comment
comment|//deserializaiton and less clunky to deal with
end_comment

begin_class
annotation|@
name|Provider
specifier|public
class|class
name|JacksonProvider
extends|extends
name|JacksonJaxbJsonProvider
block|{ }
end_class

end_unit

