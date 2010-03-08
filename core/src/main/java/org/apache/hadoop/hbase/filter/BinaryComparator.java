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
name|filter
package|;
end_package

begin_comment
comment|/**  * A binary comparator which lexicographically compares against the specified   * byte array using {@link org.apache.hadoop.hbase.util.Bytes#compareTo(byte[], byte[])}.  */
end_comment

begin_class
specifier|public
class|class
name|BinaryComparator
extends|extends
name|WritableByteArrayComparable
block|{
comment|/** Nullary constructor for Writable, do not use */
specifier|public
name|BinaryComparator
parameter_list|()
block|{ }
comment|/**    * Constructor    * @param value value    */
specifier|public
name|BinaryComparator
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

