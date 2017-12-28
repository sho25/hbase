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
name|net
package|;
end_package

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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|net
operator|.
name|HostAndPort
import|;
end_import

begin_comment
comment|/**  * An immutable type to hold a hostname and port combo, like an Endpoint  * or java.net.InetSocketAddress (but without danger of our calling  * resolve -- we do NOT want a resolve happening every time we want  * to hold a hostname and port combo). This class is also<<Comparable>>.  *<p>In implementation this class is a facade over Guava's {@link HostAndPort}.  * We cannot have Guava classes in our API hence this Type.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|Address
implements|implements
name|Comparable
argument_list|<
name|Address
argument_list|>
block|{
specifier|private
name|HostAndPort
name|hostAndPort
decl_stmt|;
specifier|private
name|Address
parameter_list|(
name|HostAndPort
name|hostAndPort
parameter_list|)
block|{
name|this
operator|.
name|hostAndPort
operator|=
name|hostAndPort
expr_stmt|;
block|}
specifier|public
specifier|static
name|Address
name|fromParts
parameter_list|(
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
block|{
return|return
operator|new
name|Address
argument_list|(
name|HostAndPort
operator|.
name|fromParts
argument_list|(
name|hostname
argument_list|,
name|port
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Address
name|fromString
parameter_list|(
name|String
name|hostnameAndPort
parameter_list|)
block|{
return|return
operator|new
name|Address
argument_list|(
name|HostAndPort
operator|.
name|fromString
argument_list|(
name|hostnameAndPort
argument_list|)
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
name|hostAndPort
operator|.
name|getHost
argument_list|()
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
name|hostAndPort
operator|.
name|getPort
argument_list|()
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
name|this
operator|.
name|hostAndPort
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
comment|// Don't use HostAndPort equals... It is wonky including
comment|// ipv6 brackets
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|other
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|other
operator|instanceof
name|Address
condition|)
block|{
name|Address
name|that
init|=
operator|(
name|Address
operator|)
name|other
decl_stmt|;
return|return
name|this
operator|.
name|getHostname
argument_list|()
operator|.
name|equals
argument_list|(
name|that
operator|.
name|getHostname
argument_list|()
argument_list|)
operator|&&
name|this
operator|.
name|getPort
argument_list|()
operator|==
name|that
operator|.
name|getPort
argument_list|()
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|this
operator|.
name|getHostname
argument_list|()
operator|.
name|hashCode
argument_list|()
operator|^
name|getPort
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Address
name|that
parameter_list|)
block|{
name|int
name|compare
init|=
name|this
operator|.
name|getHostname
argument_list|()
operator|.
name|compareTo
argument_list|(
name|that
operator|.
name|getHostname
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|compare
operator|!=
literal|0
condition|)
return|return
name|compare
return|;
return|return
name|this
operator|.
name|getPort
argument_list|()
operator|-
name|that
operator|.
name|getPort
argument_list|()
return|;
block|}
block|}
end_class

end_unit

