begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/**  * Uses an incrementing algorithm instead of the default.  */
end_comment

begin_class
specifier|public
class|class
name|IncrementingEnvironmentEdge
implements|implements
name|EnvironmentEdge
block|{
specifier|private
name|long
name|timeIncrement
init|=
literal|1
decl_stmt|;
comment|/**    * {@inheritDoc}    *<p/>    * This method increments a known value for the current time each time this    * method is called. The first value is 1.    */
annotation|@
name|Override
specifier|public
specifier|synchronized
name|long
name|currentTimeMillis
parameter_list|()
block|{
return|return
name|timeIncrement
operator|++
return|;
block|}
block|}
end_class

end_unit

