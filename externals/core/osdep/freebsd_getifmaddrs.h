/*
	this BSD code is from https://github.com/freebsd/freebsd/blob/386ddae58459341ec567604707805814a2128a57/include/ifaddrs.h
	as in older OS X there is no getifmaddrs() and related functions is NetBSD
*/

/*	$FreeBSD$	*/

/*
 * Copyright (c) 1995, 1999
 *	Berkeley Software Design, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY Berkeley Software Design, Inc. ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL Berkeley Software Design, Inc. BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *	BSDI ifaddrs.h,v 2.5 2000/02/23 14:51:59 dab Exp
 */

#ifndef    _freebsd_getifmaddrs.h_
#define    _freebsd_getifmaddrs.h_

/*
 * This may have been defined in <net/if.h>.  Note that if <net/if.h> is
 * to be included it must be included before this header file.
 */
#ifndef    ifa_broadaddr
#define    ifa_broadaddr    ifa_dstaddr    /* broadcast address interface */
#endif

struct ifmaddrs {
    struct ifmaddrs *ifma_next;
    struct sockaddr *ifma_name;
    struct sockaddr *ifma_addr;
    struct sockaddr *ifma_lladdr;
};

#include <sys/cdefs.h>


/*
 * Message format for use in obtaining information about multicast addresses
 * from the routing socket.
 */
struct ifma_msghdr {
    int ifmam_msglen;    /* to skip over non-understood messages */
    int ifmam_version;    /* future binary compatibility */
    int ifmam_type;     /* message type */
    int ifmam_addrs;    /* like rtm_addrs */
    int ifmam_flags;    /* value of ifa_flags */
    int ifmam_index;    /* index for associated ifp */
};


extern int getifaddrs(struct ifaddrs **);

extern void freeifaddrs(struct ifaddrs *);

extern int getifmaddrs(struct ifmaddrs **);

extern void freeifmaddrs(struct ifmaddrs *);

#include "freebsd_getifmaddrs.c"


#endif
