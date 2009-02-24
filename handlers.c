/*
 * $Id: handlers.c 733 2009-02-23 18:16:17Z x $
 *
 * Default handlers for different types of commands
 *
 */

#include <stdio.h>
#include <string.h>
#include <time.h>
#include <netinet/in.h>

#include "channel.h"
#include "commands.h"
#include "packet.h"
#include "util.h"


/* See comment about "the_blob" in session.c */
int handle_secret_block(PHANDLER *ph, unsigned char *payload, unsigned short len) {

	/*
	 * Actually the cache hash is sent before the server has sent any
	 * packets. It's just put here out of convenience, because this is
	 * one of the first packets ever by the server, and also not
	 * repeated during a session.
	 *
	 */

	return cmd_send_cache_hash(ph->session);
}


int handle_ping(PHANDLER *ph, unsigned char *payload, unsigned short len) {

	/* Ignore the timestamp but respond to the request */

	return cmd_ping_reply(ph->session);
}


int handle_channel(PHANDLER *ph, unsigned char *payload, unsigned short len) {
	if(ph->cmd == CMD_CHANNELERR) {
		DSFYDEBUG("handle_channel_error: Channel %d got error %d (0x%02x)\n",
			ntohs(*(unsigned short *)payload), ntohs(*(unsigned short *)(payload + 2)),
			ntohs(*(unsigned short *)(payload + 2)))
	}

	return channel_process(payload, len, ph->cmd == CMD_CHANNELERR);
}


int handle_aeskey(PHANDLER *ph, unsigned char *payload, unsigned short len) {
	CHANNEL *ch;
	int ret;

	DSFYDEBUG("Server said 0x0d (AES key) for channel %d\n", ntohs(*(unsigned short *)(payload + 2)))
	if((ch = channel_by_id(ntohs(*(unsigned short *)(payload + 2)))) != NULL) {
		ret = ch->callback(ch, payload + 4, len - 4);
		channel_unregister(ch);
	}
	else
		DSFYDEBUG("Command 0x0d: Failed to find channel with ID %d\n", ntohs(*(unsigned short *)(payload + 2)));

	return ret;
}


int handle_sha_hash(PHANDLER *ph, unsigned char *payload, unsigned short len) {

	/* Ignore */
	return 0;
}


int handle_countrycode(PHANDLER *ph, unsigned char *payload, unsigned short len) {

	/* Ignore the assigned country */

	return 0;
}


int handle_p2p_initblock(PHANDLER *ph, unsigned char *payload, unsigned short len) {
	DSFYDEBUG("%s", "Server said 0x21 (P2P initalization block)\n")

	return 0;
}


/* HTML-notification, shown in a yellow bar in the official client */
int handle_notification(PHANDLER *ph, unsigned char *payload, unsigned short len) {

	return 0;
}


/* Payload is uncompressed XML */
int handle_product_information(PHANDLER *ph, unsigned char *payload, unsigned short len) {

	return 0;
}


int handle_welcome(PHANDLER *ph, unsigned char *payload, unsigned short len) {
	unsigned char buf[64*1024];
	unsigned char hash[32+1];
	int ret;

	len = sprintf((char *)buf, "ConnectionInfo\t%d\t%s:%u\t%s", 2, ph->session->server_host, ph->session->server_port, "127.0.0.1:1080@socks5");
	if((ret = packet_write(ph->session, 0x48, buf, len)))
		return -ret;

	hex_bytes_to_ascii(ph->session->cache_hash, (char *)hash, 16);
	len = sprintf((char *)buf, "CacheReport\t%d\t%.16s\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d",
		2, hash, 0, 0, 0, 0, 0, 0, 0, 1024*1024, 0);
	if((ret = packet_write(ph->session, 0x48, buf, len)))
		return ret;

	len = sprintf((char *)buf, "ComputerInfo\t%d\t%s\t%s\t%s\t%s", 1, "", "4096", "1920x1200", "");
	ret = packet_write(ph->session, 0x48, buf, len);


	/* Request Ad with flag 0 */
	if(ret != -1)
		ret = cmd_requestad(ph->session, 0);


	/* Request Ad with flag 1 */
	if(ret == 0)
		ret = cmd_requestad(ph->session, 1);


	return ret;
}
