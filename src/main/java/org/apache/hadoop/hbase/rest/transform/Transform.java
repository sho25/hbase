begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *   http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|transform
package|;
end_package

begin_comment
comment|/**  * Data transformation module  */
end_comment

begin_interface
specifier|public
interface|interface
name|Transform
block|{
comment|/*** Transfer direction */
specifier|static
enum|enum
name|Direction
block|{
comment|/** From client to server */
name|IN
block|,
comment|/** From server to client */
name|OUT
block|}
empty_stmt|;
comment|/**    * Transform data from one representation to another according to    * transfer direction.    * @param in input data    * @param direction IN or OUT    * @return the transformed data    */
name|byte
index|[]
name|transform
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|Direction
name|direction
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

