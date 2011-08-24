begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

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
name|Addressing
import|;
end_import

begin_comment
comment|/**  * Data structure to hold HRegionInfo and the address for the hosting  * HRegionServer.  Immutable.  Comparable, but we compare the 'location' only:  * i.e. the hostname and port, and *not* the regioninfo.  This means two  * instances are the same if they refer to the same 'location' (the same  * hostname and port), though they may be carrying different regions.  */
end_comment

begin_class
specifier|public
class|class
name|HRegionLocation
implements|implements
name|Comparable
argument_list|<
name|HRegionLocation
argument_list|>
block|{
specifier|private
specifier|final
name|HRegionInfo
name|regionInfo
decl_stmt|;
specifier|private
specifier|final
name|String
name|hostname
decl_stmt|;
specifier|private
specifier|final
name|int
name|port
decl_stmt|;
comment|// Cache of the 'toString' result.
specifier|private
name|String
name|cachedString
init|=
literal|null
decl_stmt|;
comment|// Cache of the hostname + port
specifier|private
name|String
name|cachedHostnamePort
decl_stmt|;
comment|/**    * Constructor    * @param regionInfo the HRegionInfo for the region    * @param hostname Hostname    * @param port port    */
specifier|public
name|HRegionLocation
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|String
name|hostname
parameter_list|,
specifier|final
name|int
name|port
parameter_list|)
block|{
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
name|this
operator|.
name|hostname
operator|=
name|hostname
expr_stmt|;
name|this
operator|.
name|port
operator|=
name|port
expr_stmt|;
block|}
comment|/**    * @see java.lang.Object#toString()    */
annotation|@
name|Override
specifier|public
specifier|synchronized
name|String
name|toString
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|cachedString
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|cachedString
operator|=
literal|"region="
operator|+
name|this
operator|.
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|", hostname="
operator|+
name|this
operator|.
name|hostname
operator|+
literal|", port="
operator|+
name|this
operator|.
name|port
expr_stmt|;
block|}
return|return
name|this
operator|.
name|cachedString
return|;
block|}
comment|/**    * @see java.lang.Object#equals(java.lang.Object)    */
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|HRegionLocation
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|this
operator|.
name|compareTo
argument_list|(
operator|(
name|HRegionLocation
operator|)
name|o
argument_list|)
operator|==
literal|0
return|;
block|}
comment|/**    * @see java.lang.Object#hashCode()    */
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|this
operator|.
name|hostname
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|^=
name|this
operator|.
name|port
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/** @return HRegionInfo */
specifier|public
name|HRegionInfo
name|getRegionInfo
parameter_list|()
block|{
return|return
name|regionInfo
return|;
block|}
comment|/**    * Do not use!!! Creates a HServerAddress instance which will do a resolve.    * @return HServerAddress    * @deprecated Use {@link #getHostnamePort}    */
specifier|public
name|HServerAddress
name|getServerAddress
parameter_list|()
block|{
return|return
operator|new
name|HServerAddress
argument_list|(
name|this
operator|.
name|hostname
argument_list|,
name|this
operator|.
name|port
argument_list|)
return|;
block|}
specifier|public
name|String
name|getHostname
parameter_list|()
block|{
return|return
name|this
operator|.
name|hostname
return|;
block|}
specifier|public
name|int
name|getPort
parameter_list|()
block|{
return|return
name|this
operator|.
name|port
return|;
block|}
comment|/**    * @return String made of hostname and port formatted as per {@link Addressing#createHostAndPortStr(String, int)}    */
specifier|public
specifier|synchronized
name|String
name|getHostnamePort
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|cachedHostnamePort
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|cachedHostnamePort
operator|=
name|Addressing
operator|.
name|createHostAndPortStr
argument_list|(
name|this
operator|.
name|hostname
argument_list|,
name|this
operator|.
name|port
argument_list|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|cachedHostnamePort
return|;
block|}
comment|//
comment|// Comparable
comment|//
specifier|public
name|int
name|compareTo
parameter_list|(
name|HRegionLocation
name|o
parameter_list|)
block|{
name|int
name|result
init|=
name|this
operator|.
name|hostname
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|getHostname
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
return|return
name|result
return|;
return|return
name|this
operator|.
name|port
operator|-
name|o
operator|.
name|getPort
argument_list|()
return|;
block|}
block|}
end_class

end_unit

